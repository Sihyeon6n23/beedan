package com.goodee.beedan.controller.quote;

import com.goodee.beedan.common.constant.TransportType;
import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.entity.*;
import com.goodee.beedan.repository.member.MemberRepository;
import com.goodee.beedan.repository.quote.FactoryRepository;
import com.goodee.beedan.repository.quote.HsCodeRepository;
import com.goodee.beedan.repository.stock.StockRepository;
import com.goodee.beedan.service.buyer.BuyerService;
import com.goodee.beedan.service.buyer.FeePolicyService;
import com.goodee.beedan.service.quote.DomesticDeliveryRateService;
import com.goodee.beedan.service.quote.PortCustomsRateService;
import com.goodee.beedan.service.quote.QuoteBaseService;
import com.goodee.beedan.service.quote.QuoteDetailService;
import com.goodee.beedan.service.quote.ShippingRateService;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/quote")
@RequiredArgsConstructor
public class QuoteRestController {

    private final DomesticDeliveryRateService domesticDeliveryRateService;
    private final ShippingRateService shippingRateService;
    private final PortCustomsRateService portCustomsRateService;
    private final QuoteBaseService quoteBaseService;
    private final QuoteDetailService quoteDetailService;
    private final FactoryRepository factoryRepository;
    private final HsCodeRepository hsCodeRepository;
    private final StockRepository stockRepository;
    private final MemberRepository memberRepository;
    private final BuyerService buyerService;
    private final FeePolicyService feePolicyService;
    private final com.goodee.beedan.repository.receiver.ReceiverRepository receiverRepository;
    private final com.goodee.beedan.repository.quote.QuoteInfoRepository quoteInfoRepository;

    private static final Map<String, String> REGION_NAMES = Map.of(
            "SEOUL", "서울특별시",
            "GYEONGGI", "경기도",
            "METRO", "수도권 (인천·세종·대전)",
            "PROVINCE", "지방"
    );

    // ── 국내 배달비 계산 (지역별 그룹핑) ──────────────
    @PostMapping("/delivery-fee")
    public ResponseEntity<DeliveryFeeResponse> calculateDeliveryFee(
            @RequestBody DeliveryFeeRequest request) {

        if (request.getItems() == null || request.getItems().isEmpty()) {
            return ResponseEntity.ok(DeliveryFeeResponse.empty());
        }

        // 지역별 그룹핑
        Map<String, Integer> regionCounts = new LinkedHashMap<>();
        for (DeliveryFeeRequest.Item item : request.getItems()) {
            String rgn = item.getRegion();
            if (rgn != null && !rgn.isEmpty()) {
                regionCounts.merge(rgn, 1, Integer::sum);
            }
        }

        if (regionCounts.isEmpty()) {
            return ResponseEntity.ok(DeliveryFeeResponse.empty());
        }

        try {
            List<DeliveryFeeResponse.RegionGroup> groups = new ArrayList<>();
            BigDecimal totalFee = BigDecimal.ZERO;
            int totalCount = 0;

            for (Map.Entry<String, Integer> entry : regionCounts.entrySet()) {
                String rgn = entry.getKey();
                int count = entry.getValue();
                DomesticDeliveryRate rate = domesticDeliveryRateService.findActiveByRegion(rgn);

                BigDecimal baseFeeUnit = rate.getDdrAm();
                BigDecimal extraFeeUnit = rate.getDdrEAm() != null ? rate.getDdrEAm() : BigDecimal.ZERO;
                BigDecimal subtotal = baseFeeUnit.add(extraFeeUnit).multiply(BigDecimal.valueOf(count));

                groups.add(DeliveryFeeResponse.RegionGroup.builder()
                        .regionCode(rgn)
                        .regionName(REGION_NAMES.getOrDefault(rgn, rgn))
                        .count(count)
                        .baseFeeUnit(baseFeeUnit)
                        .extraFeeUnit(extraFeeUnit)
                        .subtotal(subtotal)
                        .build());

                totalFee = totalFee.add(subtotal);
                totalCount += count;
            }

            return ResponseEntity.ok(DeliveryFeeResponse.builder()
                    .regions(groups)
                    .totalFee(totalFee)
                    .totalCount(totalCount)
                    .build());

        } catch (Exception e) {
            return ResponseEntity.ok(DeliveryFeeResponse.empty());
        }
    }

    // ── 임시저장 ─────────────────────────────────────
    @PostMapping("/draft")
    public ResponseEntity<Map<String, Object>> saveDraft(
            @RequestBody DraftRequest request) {

        try {
            QuoteBase quoteBase = quoteBaseService.findById(request.getQuId());
            if (!quoteBase.isEditable()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("status", "error", "message", "수정 불가 상태입니다."));
            }

            // 1. QuoteInfo 생성 또는 갱신
            QuoteInfo quoteInfo = quoteInfoRepository.findByQuId(request.getQuId()).orElse(null);
            if (quoteInfo == null) {
                quoteInfo = QuoteInfo.builder()
                        .quoteId(request.getQuId())
                        .negoId(quoteBase.getNgId())
                        .currencyCode(null)
                        .exchangeRate(null)
                        .buyerGradePolicyId(null)
                        .feePolicyId(null)
                        .ps(request.getMemo())
                        .desiredDate(null)
                        .build();
            }
            quoteInfo.updateDraft(request.getMemo(), request.getSiId(), request.getStiId());
            quoteInfo = quoteInfoRepository.save(quoteInfo);

            // 2. QuoteDetail 삭제 후 재저장
            List<QuoteDetail> existing = quoteDetailService.findAllByQuote(request.getQuId());
            existing.forEach(d -> quoteDetailService.delete(d.getQuDtId()));

            for (DraftRequest.DraftItem item : request.getItems()) {
                Stock stock = stockRepository.findById(item.getStId()).orElse(null);
                if (stock == null) continue;

                QuoteDetail detail = QuoteDetail.builder()
                        .quoteInfoId(quoteInfo.getQuInfoId())
                        .quoteId(request.getQuId())
                        .negoId(quoteBase.getNgId())
                        .stockId(item.getStId())
                        .stockQuantity(item.getQty())
                        .stockName(stock.getStNm())
                        .unitGroupId(item.getUnGId())
                        .unitGroupName(item.getUnGNm())
                        .unitGroupQuantity(item.getUnGQn())
                        .foreignPrice(stock.getStPr())
                        .krwTotal(item.getSubtotalKrw())
                        .receiverId(item.getRcId())
                        .build();
                quoteDetailService.save(detail);
            }

            return ResponseEntity.ok(Map.of(
                    "status", "ok",
                    "message", "임시저장 완료",
                    "quId", request.getQuId()
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "error", "message", "저장 실패: " + e.getMessage()));
        }
    }

    // ── 예상 운임 비용 전체 계산 (공장별 그룹핑) ──────
    @PostMapping("/estimate-fees")
    public ResponseEntity<EstimateFeeResponse> estimateFees(
            @RequestBody EstimateFeeRequest request,
            @AuthenticationPrincipal MemberUserDetails userDetails) {

        try {
            List<EstimateItem> items = request.getItems();
            if (items == null) items = Collections.emptyList();

            boolean insured = request.getInsuranceYn() != null && request.getInsuranceYn();
            BigDecimal insuranceRate = request.getInsuranceRate() != null
                    ? request.getInsuranceRate() : BigDecimal.ZERO;

            // ── 1. 상품을 공장별로 그룹핑 ──────────────
            // key: faId (공장 ID), value: (Factory, List<EstimateItem>)
            Map<Long, List<EstimateItem>> factoryItemMap = new LinkedHashMap<>();
            Map<Long, Factory> factoryMap = new HashMap<>();

            for (EstimateItem item : items) {
                if (item.getStId() == null) continue;
                Stock stock = stockRepository.findById(item.getStId()).orElse(null);
                if (stock == null || stock.getBrId() == null) continue;

                List<Factory> factories = factoryRepository.findAllByBrIdAndFaYnTrue(stock.getBrId());
                Factory factory = factories.isEmpty() ? null : factories.get(0);

                Long faKey = factory != null ? factory.getFaId() : -1L;
                if (factory != null) factoryMap.putIfAbsent(faKey, factory);
                factoryItemMap.computeIfAbsent(faKey, k -> new ArrayList<>()).add(item);
            }

            // ── 2. 공장별 운임 계산 ──────────────────
            List<FactoryFeeGroup> factoryGroups = new ArrayList<>();

            BigDecimal totalShipping = BigDecimal.ZERO;
            BigDecimal totalPort = BigDecimal.ZERO;
            BigDecimal totalCustoms = BigDecimal.ZERO;
            BigDecimal totalHsCode = BigDecimal.ZERO;
            BigDecimal totalInsurance = BigDecimal.ZERO;
            BigDecimal totalCif = BigDecimal.ZERO;
            BigDecimal totalDuty = BigDecimal.ZERO;
            BigDecimal totalVat = BigDecimal.ZERO;
            BigDecimal totalLogistics = BigDecimal.ZERO;

            for (Map.Entry<Long, List<EstimateItem>> entry : factoryItemMap.entrySet()) {
                Factory factory = factoryMap.get(entry.getKey());
                List<EstimateItem> groupItems = entry.getValue();

                String countryCode = factory != null ? factory.getFaCCd() : null;
                String factoryName = factory != null ? factory.getFaNm() : "알 수 없음";
                String factoryCity = factory != null ? factory.getFaCty() : null;

                // 그룹 내 합산
                int groupDozen = 0;
                BigDecimal groupSupply = BigDecimal.ZERO;
                BigDecimal dutyRateSum = BigDecimal.ZERO;
                int dutyRateCount = 0;

                for (EstimateItem gi : groupItems) {
                    groupDozen += gi.getDozen();
                    if (gi.getSupplyKrw() != null) groupSupply = groupSupply.add(gi.getSupplyKrw());
                    if (gi.getDutyRate() != null && gi.getDutyRate().compareTo(BigDecimal.ZERO) > 0) {
                        dutyRateSum = dutyRateSum.add(gi.getDutyRate());
                        dutyRateCount++;
                    }
                }

                BigDecimal avgDutyRate = dutyRateCount > 0
                        ? dutyRateSum.divide(BigDecimal.valueOf(dutyRateCount), 4, RoundingMode.HALF_UP)
                        : new BigDecimal("0.13");

                // 해외 운임
                BigDecimal grpShipping = BigDecimal.ZERO;
                String transportType = null;
                String sizeType = "SMALL";
                ShippingRate sr = null;
                LocalDateTime srUpdatedAt = null;

                if (countryCode != null) {
                    try {
                        sr = shippingRateService.findByCCdAndTransportType(countryCode, TransportType.SEA);
                        grpShipping = sr.getApplicableAmount(groupDozen);
                        sizeType = sr.getSizeType(groupDozen);
                        transportType = sr.getSrTrspTy().name();
                        srUpdatedAt = sr.getSrUpDt();
                    } catch (Exception ignored) {}
                }

                // 항만/통관/HS
                BigDecimal grpPort = BigDecimal.ZERO;
                BigDecimal grpCustoms = BigDecimal.ZERO;
                BigDecimal grpHsCode = BigDecimal.ZERO;
                LocalDateTime pcrUpdatedAt = null;

                try {
                    PortCustomsRate pr = portCustomsRateService.findActiveByType("PORT");
                    grpPort = pr.getApplicableAmount(sizeType);
                    pcrUpdatedAt = pr.getPcrUpDt();
                } catch (Exception ignored) {}
                try { grpCustoms = portCustomsRateService.findActiveByType("CUSTOMS").getApplicableAmount(sizeType); } catch (Exception ignored) {}
                try { grpHsCode = portCustomsRateService.findActiveByType("HS_CODE").getApplicableAmount(sizeType); } catch (Exception ignored) {}

                // 보험
                BigDecimal grpInsurance = BigDecimal.ZERO;
                if (insured && groupSupply.compareTo(BigDecimal.ZERO) > 0) {
                    grpInsurance = groupSupply.multiply(insuranceRate).setScale(0, RoundingMode.HALF_UP);
                }

                // CIF
                BigDecimal grpCif = groupSupply.add(grpShipping).add(grpInsurance);

                // 관세
                BigDecimal grpDuty = grpCif.multiply(avgDutyRate).setScale(0, RoundingMode.HALF_UP);

                // 부가세
                BigDecimal grpVat = grpCif.add(grpDuty).multiply(new BigDecimal("0.10")).setScale(0, RoundingMode.HALF_UP);

                // 그룹 소계
                BigDecimal grpSubtotal = grpShipping.add(grpPort).add(grpCustoms).add(grpHsCode)
                        .add(grpInsurance).add(grpDuty).add(grpVat);

                factoryGroups.add(FactoryFeeGroup.builder()
                        .factoryName(factoryName)
                        .factoryCity(factoryCity)
                        .countryCode(countryCode)
                        .transportType(transportType)
                        .sizeType(sizeType)
                        .itemCount(groupItems.size())
                        .totalDozen(groupDozen)
                        .supplySubtotal(groupSupply)
                        .shippingFee(grpShipping)
                        .portFee(grpPort)
                        .customsFee(grpCustoms)
                        .hsCodeFee(grpHsCode)
                        .insuranceFee(grpInsurance)
                        .cifAmount(grpCif)
                        .dutyRate(avgDutyRate)
                        .dutyAmount(grpDuty)
                        .vatAmount(grpVat)
                        .subtotal(grpSubtotal)
                        .srSmQn(sr != null ? sr.getSrSmQn() : null)
                        .srSmAm(sr != null ? sr.getSrSmAm() : null)
                        .srMdQn(sr != null ? sr.getSrMdQn() : null)
                        .srMdAm(sr != null ? sr.getSrMdAm() : null)
                        .srLgQn(sr != null ? sr.getSrLgQn() : null)
                        .srLgAm(sr != null ? sr.getSrLgAm() : null)
                        .srSince(sr != null ? fmtDate(sr.getSrCrDt()) : null)
                        .shippingRateUpdatedAt(fmtDate(srUpdatedAt))
                        .portCustomsRateUpdatedAt(fmtDate(pcrUpdatedAt))
                        .build());

                // 총계 누적
                totalShipping = totalShipping.add(grpShipping);
                totalPort = totalPort.add(grpPort);
                totalCustoms = totalCustoms.add(grpCustoms);
                totalHsCode = totalHsCode.add(grpHsCode);
                totalInsurance = totalInsurance.add(grpInsurance);
                totalCif = totalCif.add(grpCif);
                totalDuty = totalDuty.add(grpDuty);
                totalVat = totalVat.add(grpVat);
                totalLogistics = totalLogistics.add(grpSubtotal);
            }

            // ── 3. 구매 대행 수수료 (공장 무관) ─────────
            BigDecimal serviceFee = BigDecimal.ZERO;
            BigDecimal docFee = BigDecimal.ZERO;
            String buyerGrade = "STANDARD";

            if (userDetails != null) {
                try {
                    Member member = memberRepository.findByMemLgnId(userDetails.getUsername()).orElse(null);
                    if (member != null && member.getMemBizNo() != null) {
                        Buyer buyer = buyerService.findByBizNo(member.getMemBizNo());
                        buyerGrade = buyer.getBgpGr();
                    }
                } catch (Exception ignored) {}
            }

            LocalDateTime svcEffFrom = null, svcEffTo = null;
            LocalDateTime docEffFrom = null, docEffTo = null;

            try {
                FeePolicy commissionPolicy = feePolicyService
                        .findAllActiveByGrade(buyerGrade).stream()
                        .filter(fp -> "SERVICE_COMMISSION".equals(fp.getFpFeeTy()))
                        .filter(FeePolicy::isValid)
                        .findFirst().orElse(null);
                if (commissionPolicy != null) {
                    if (request.getItemTotalKrw() != null) {
                        serviceFee = commissionPolicy.apply(request.getItemTotalKrw());
                    }
                    svcEffFrom = commissionPolicy.getFpEfFrDt();
                    svcEffTo = commissionPolicy.getFpEfToDt();
                }
            } catch (Exception ignored) {}

            try {
                FeePolicy docPolicy = feePolicyService
                        .findAllActiveByGrade(buyerGrade).stream()
                        .filter(fp -> "DOCUMENT".equals(fp.getFpFeeTy()))
                        .filter(FeePolicy::isValid)
                        .findFirst().orElse(null);
                if (docPolicy != null) {
                    docFee = docPolicy.apply(request.getItemTotalKrw() != null
                            ? request.getItemTotalKrw() : BigDecimal.ZERO);
                    docEffFrom = docPolicy.getFpEfFrDt();
                    docEffTo = docPolicy.getFpEfToDt();
                }
            } catch (Exception ignored) {}

            BigDecimal procurementTotal = serviceFee.add(docFee);

            // ── 3-1. 등급 할인 정보 ─────────────────
            BigDecimal shippingDiscountRate = BigDecimal.ZERO;
            try {
                FeePolicy shippingPolicy = feePolicyService
                        .findAllActiveByGrade(buyerGrade).stream()
                        .filter(fp -> "SHIPPING".equals(fp.getFpFeeTy()))
                        .filter(FeePolicy::isValid)
                        .findFirst().orElse(null);
                if (shippingPolicy != null) {
                    shippingDiscountRate = shippingPolicy.getFpVal();
                }
            } catch (Exception ignored) {}

            BigDecimal standardServiceFee = null;
            if (!"STANDARD".equals(buyerGrade)) {
                try {
                    FeePolicy standardCommission = feePolicyService
                            .findAllActiveByGrade("STANDARD").stream()
                            .filter(fp -> "SERVICE_COMMISSION".equals(fp.getFpFeeTy()))
                            .filter(FeePolicy::isValid)
                            .findFirst().orElse(null);
                    if (standardCommission != null && request.getItemTotalKrw() != null) {
                        standardServiceFee = standardCommission.apply(request.getItemTotalKrw());
                    }
                } catch (Exception ignored) {}
            }

            // ── 4. 국내 배달비 계산 ─────────────────
            List<String> shipRegions = request.getShipRegions();
            // shipRegions가 없으면 사용자 기본 수령지로 상품 수만큼 세팅
            if (shipRegions == null || shipRegions.isEmpty()) {
                String fallbackRegion = "SEOUL";
                if (userDetails != null) {
                    try {
                        com.goodee.beedan.entity.Member member = memberRepository.findByMemLgnId(userDetails.getUsername()).orElse(null);
                        if (member != null) {
                            com.goodee.beedan.entity.Receiver defaultRcv = receiverRepository.findFirstByMember_memIdAndRcAdrDfYnTrueAndRcDelYnFalse(member.getMemId());
                            if (defaultRcv != null && defaultRcv.getRcRgn() != null) {
                                fallbackRegion = defaultRcv.getRcRgn();
                            }
                        }
                    } catch (Exception ignored) {}
                }
                // 공장 그룹 수 = 국내 배송 건수 (출발지가 다르면 별도 배송)
                int shipCount = Math.max(factoryGroups.size(), items.isEmpty() ? 0 : 1);
                shipRegions = new ArrayList<>();
                for (int i = 0; i < shipCount; i++) shipRegions.add(fallbackRegion);
            }

            // 지역별 그룹핑
            List<DeliveryFeeResponse.RegionGroup> domesticRegions = new ArrayList<>();
            BigDecimal domesticFee = BigDecimal.ZERO;
            int domesticCount = 0;
            Map<String, Integer> regionCounts = new LinkedHashMap<>();
            for (String rgn : shipRegions) {
                if (rgn != null && !rgn.isEmpty()) regionCounts.merge(rgn, 1, Integer::sum);
            }
            for (Map.Entry<String, Integer> rc : regionCounts.entrySet()) {
                try {
                    DomesticDeliveryRate ddr = domesticDeliveryRateService.findActiveByRegion(rc.getKey());
                    BigDecimal base = ddr.getDdrAm();
                    BigDecimal extra = ddr.getDdrEAm() != null ? ddr.getDdrEAm() : BigDecimal.ZERO;
                    BigDecimal sub = base.add(extra).multiply(BigDecimal.valueOf(rc.getValue()));
                    domesticRegions.add(DeliveryFeeResponse.RegionGroup.builder()
                            .regionCode(rc.getKey())
                            .regionName(REGION_NAMES.getOrDefault(rc.getKey(), rc.getKey()))
                            .count(rc.getValue())
                            .baseFeeUnit(base)
                            .extraFeeUnit(extra)
                            .subtotal(sub)
                            .build());
                    domesticFee = domesticFee.add(sub);
                    domesticCount += rc.getValue();
                } catch (Exception ignored) {}
            }

            return ResponseEntity.ok(EstimateFeeResponse.builder()
                    .factories(factoryGroups)
                    .shippingFee(totalShipping)
                    .portFee(totalPort)
                    .customsFee(totalCustoms)
                    .hsCodeFee(totalHsCode)
                    .insuranceFee(totalInsurance)
                    .cifAmount(totalCif)
                    .dutyAmount(totalDuty)
                    .vatAmount(totalVat)
                    .logisticsTotal(totalLogistics)
                    .buyerGrade(buyerGrade)
                    .serviceFee(serviceFee)
                    .docFee(docFee)
                    .procurementTotal(procurementTotal)
                    .serviceFeeEffFrom(fmtDate(svcEffFrom))
                    .serviceFeeEffTo(fmtDate(svcEffTo))
                    .docFeeEffFrom(fmtDate(docEffFrom))
                    .docFeeEffTo(fmtDate(docEffTo))
                    .shippingDiscountRate(shippingDiscountRate)
                    .standardServiceFee(standardServiceFee)
                    .domesticRegions(domesticRegions)
                    .domesticFee(domesticFee)
                    .domesticCount(domesticCount)
                    .build());

        } catch (Exception e) {
            return ResponseEntity.ok(EstimateFeeResponse.empty());
        }
    }

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private static String fmtDate(LocalDateTime dt) {
        return dt != null ? dt.format(DATE_FMT) : null;
    }

    // ── Request / Response DTOs ──────────────────────

    @Getter
    @NoArgsConstructor
    public static class DeliveryFeeRequest {
        private List<Item> items;

        @Getter
        @NoArgsConstructor
        public static class Item {
            private String region;
            private int qty;
            private boolean splitShipment;
        }
    }

    @Getter
    @Builder
    public static class DeliveryFeeResponse {
        private List<RegionGroup> regions;
        private BigDecimal totalFee;
        private int totalCount;

        @Getter
        @Builder
        public static class RegionGroup {
            private String regionCode;
            private String regionName;
            private int count;
            private BigDecimal baseFeeUnit;   // 건당 기본 배달비
            private BigDecimal extraFeeUnit;  // 건당 추가금
            private BigDecimal subtotal;      // (기본+추가) × 건수
        }

        public static DeliveryFeeResponse empty() {
            return DeliveryFeeResponse.builder()
                    .regions(Collections.emptyList())
                    .totalFee(BigDecimal.ZERO)
                    .totalCount(0).build();
        }
    }

    @Getter
    @NoArgsConstructor
    public static class DraftRequest {
        private Long quId;
        private List<DraftItem> items;
        private String memo;
        private Long siId;          // 선택한 보험 ID
        private Long stiId;         // 선택한 검사 ID

        @Getter
        @NoArgsConstructor
        public static class DraftItem {
            private Long stId;
            private Integer qty;
            private Long unGId;
            private String unGNm;
            private Integer unGQn;
            private BigDecimal subtotalKrw;  // 사용자 조정 소계 (한화)
            private Long rcId;               // 수령지 ID
        }
    }

    // ── 운임 예상 요청: 상품별 상세 ─────────────────
    @Getter
    @NoArgsConstructor
    public static class EstimateFeeRequest {
        private List<EstimateItem> items;
        private BigDecimal itemTotalKrw;      // 사용자 조정 소계 합산 (수수료용)
        private Boolean insuranceYn;
        private BigDecimal insuranceRate;     // 선택한 보험 요율
        private List<String> shipRegions;     // 배송지 지역 코드 (없으면 기본 수령지 사용)
    }

    @Getter
    @NoArgsConstructor
    public static class EstimateItem {
        private Long stId;
        private int qty;
        private int dozen;
        private BigDecimal supplyKrw;   // stPr × qty × 환율
        private BigDecimal dutyRate;    // hsDuRa
    }

    // ── 운임 예상 응답: 공장별 그룹 + 합산 ──────────
    @Getter
    @Builder
    public static class FactoryFeeGroup {
        private String factoryName;
        private String factoryCity;
        private String countryCode;
        private String transportType;
        private String sizeType;
        private int itemCount;
        private int totalDozen;
        private BigDecimal supplySubtotal;
        private BigDecimal shippingFee;
        private BigDecimal portFee;
        private BigDecimal customsFee;
        private BigDecimal hsCodeFee;
        private BigDecimal insuranceFee;
        private BigDecimal cifAmount;
        private BigDecimal dutyRate;
        private BigDecimal dutyAmount;
        private BigDecimal vatAmount;
        private BigDecimal subtotal;
        // 운임 구간표
        private Integer srSmQn;
        private BigDecimal srSmAm;
        private Integer srMdQn;
        private BigDecimal srMdAm;
        private Integer srLgQn;
        private BigDecimal srLgAm;
        private String srSince;
        private String shippingRateUpdatedAt;
        private String portCustomsRateUpdatedAt;
    }

    @Getter
    @Builder
    public static class EstimateFeeResponse {
        private List<FactoryFeeGroup> factories;
        // 합산 총계
        private BigDecimal shippingFee;
        private BigDecimal portFee;
        private BigDecimal customsFee;
        private BigDecimal hsCodeFee;
        private BigDecimal insuranceFee;
        private BigDecimal cifAmount;
        private BigDecimal dutyAmount;
        private BigDecimal vatAmount;
        private BigDecimal logisticsTotal;
        // 대행 수수료
        private String buyerGrade;
        private BigDecimal serviceFee;
        private BigDecimal docFee;
        private BigDecimal procurementTotal;
        // 대행 정책 기간
        private String serviceFeeEffFrom;
        private String serviceFeeEffTo;
        private String docFeeEffFrom;
        private String docFeeEffTo;
        // 등급 할인
        private BigDecimal shippingDiscountRate;   // SHIPPING fee policy rate (0이면 할인 없음)
        private BigDecimal standardServiceFee;     // STANDARD 기준 서비스 수수료 (비교용, null이면 STANDARD)
        // 국내 배달비
        private List<DeliveryFeeResponse.RegionGroup> domesticRegions;
        private BigDecimal domesticFee;
        private int domesticCount;

        public static EstimateFeeResponse empty() {
            return EstimateFeeResponse.builder()
                    .factories(Collections.emptyList())
                    .shippingFee(BigDecimal.ZERO).portFee(BigDecimal.ZERO)
                    .customsFee(BigDecimal.ZERO).hsCodeFee(BigDecimal.ZERO)
                    .insuranceFee(BigDecimal.ZERO).cifAmount(BigDecimal.ZERO)
                    .dutyAmount(BigDecimal.ZERO).vatAmount(BigDecimal.ZERO)
                    .logisticsTotal(BigDecimal.ZERO)
                    .serviceFee(BigDecimal.ZERO).docFee(BigDecimal.ZERO)
                    .procurementTotal(BigDecimal.ZERO)
                    .domesticRegions(Collections.emptyList())
                    .domesticFee(BigDecimal.ZERO).domesticCount(0)
                    .build();
        }
    }
}
