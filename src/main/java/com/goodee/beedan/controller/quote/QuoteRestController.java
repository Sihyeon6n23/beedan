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
import java.util.List;
import java.util.Map;

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

    private static final Map<String, String> REGION_NAMES = Map.of(
            "SEOUL", "서울특별시",
            "GYEONGGI", "경기도",
            "METRO", "수도권 (인천·세종·대전)",
            "PROVINCE", "지방"
    );

    // ── 국내 배달비 계산 ──────────────────────────────
    @PostMapping("/delivery-fee")
    public ResponseEntity<DeliveryFeeResponse> calculateDeliveryFee(
            @RequestBody DeliveryFeeRequest request) {

        String region = null;
        int shipmentCount = 0;

        for (DeliveryFeeRequest.Item item : request.getItems()) {
            if (item.getRegion() != null && !item.getRegion().isEmpty()) {
                if (region == null) region = item.getRegion();
                shipmentCount++;
            }
        }

        if (region == null) {
            return ResponseEntity.ok(DeliveryFeeResponse.empty());
        }

        try {
            DomesticDeliveryRate rate = domesticDeliveryRateService.findActiveByRegion(region);
            BigDecimal baseFee = rate.getDdrAm().multiply(BigDecimal.valueOf(shipmentCount));
            BigDecimal extraFee = rate.getDdrEAm() != null
                    ? rate.getDdrEAm().multiply(BigDecimal.valueOf(shipmentCount))
                    : BigDecimal.ZERO;
            BigDecimal totalFee = baseFee.add(extraFee);

            return ResponseEntity.ok(DeliveryFeeResponse.builder()
                    .regionName(REGION_NAMES.getOrDefault(region, region))
                    .baseFee(baseFee)
                    .extraFee(extraFee)
                    .totalFee(totalFee)
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

            // 기존 QuoteDetail 삭제 후 재저장
            List<QuoteDetail> existing = quoteDetailService.findAllByQuote(request.getQuId());
            existing.forEach(d -> quoteDetailService.delete(d.getQuDtId()));

            Negotiation negotiation = null;
            try {
                // ngId 조회 - QuoteBase에서 가져옴
            } catch (Exception ignored) {}

            for (DraftRequest.DraftItem item : request.getItems()) {
                Stock stock = stockRepository.findById(item.getStId()).orElse(null);
                if (stock == null) continue;

                quoteDetailService.create(
                        com.goodee.beedan.dto.quote.QuoteDetailRequest.builder()
                                .quInfoId(null)
                                .quId(request.getQuId())
                                .ngId(quoteBase.getNgId())
                                .stId(item.getStId())
                                .quDtQn(item.getQty())
                                .stNm(stock.getStNm())
                                .unGId(item.getUnGId())
                                .unGNm(item.getUnGNm())
                                .quUQn(item.getUnGQn())
                                .quDtFgPr(stock.getStPr())
                                .build()
                );
            }

            // 메모 저장은 QuoteBase.quCon에
            // TODO: QuoteBase에 메모 setter 추가 필요 시

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

    // ── 예상 운임 비용 전체 계산 ──────────────────────
    @PostMapping("/estimate-fees")
    public ResponseEntity<EstimateFeeResponse> estimateFees(
            @RequestBody EstimateFeeRequest request,
            @AuthenticationPrincipal MemberUserDetails userDetails) {

        try {
            // 1. 총 다스 수 계산
            int totalDozen = request.getTotalDozen();

            // 2. 해상 운임: Stock.brId → Factory.faCCd → ShippingRate
            BigDecimal shippingFee = BigDecimal.ZERO;
            String countryCode = null;
            String sizeType = "SMALL";

            if (request.getStIds() != null && !request.getStIds().isEmpty()) {
                for (Long stId : request.getStIds()) {
                    Stock stock = stockRepository.findById(stId).orElse(null);
                    if (stock == null || stock.getBrId() == null) continue;

                    List<Factory> factories = factoryRepository.findAllByBrIdAndFaYnTrue(stock.getBrId());
                    if (!factories.isEmpty()) {
                        countryCode = factories.get(0).getFaCCd();
                        break;
                    }
                }
            }

            String factoryName = null;
            String factoryCity = null;
            String transportTypeName = null;

            if (countryCode != null) {
                try {
                    // 공장 정보 저장
                    for (Long stId : request.getStIds()) {
                        Stock stock = stockRepository.findById(stId).orElse(null);
                        if (stock == null || stock.getBrId() == null) continue;
                        List<Factory> facs = factoryRepository.findAllByBrIdAndFaYnTrue(stock.getBrId());
                        if (!facs.isEmpty()) {
                            factoryName = facs.get(0).getFaNm();
                            factoryCity = facs.get(0).getFaCty();
                            break;
                        }
                    }

                    ShippingRate sr = shippingRateService.findByCCdAndTransportType(
                            countryCode, TransportType.SEA);
                    shippingFee = sr.getApplicableAmount(totalDozen);
                    sizeType = sr.getSizeType(totalDozen);
                    transportTypeName = sr.getSrTrspTy().name();
                } catch (Exception ignored) {}
            }

            // 3. 항만 비용 + 통관 수수료 + HS Code 신고료
            BigDecimal portFee = BigDecimal.ZERO;
            BigDecimal customsFee = BigDecimal.ZERO;
            BigDecimal hsCodeFee = BigDecimal.ZERO;
            try {
                portFee = portCustomsRateService.findActiveByType("PORT")
                        .getApplicableAmount(sizeType);
            } catch (Exception ignored) {}
            try {
                customsFee = portCustomsRateService.findActiveByType("CUSTOMS")
                        .getApplicableAmount(sizeType);
            } catch (Exception ignored) {}
            try {
                hsCodeFee = portCustomsRateService.findActiveByType("HS_CODE")
                        .getApplicableAmount(sizeType);
            } catch (Exception ignored) {}

            BigDecimal portCustomsTotal = portFee.add(customsFee).add(hsCodeFee);

            // 4. 보험료 (예상: 상품가의 0.5%)
            BigDecimal insuranceFee = BigDecimal.ZERO;
            if (request.getItemTotalKrw() != null) {
                insuranceFee = request.getItemTotalKrw()
                        .multiply(new BigDecimal("0.005"))
                        .setScale(0, RoundingMode.HALF_UP);
            }

            // 5. CIF = 상품가 + 해외운임 + 보험료
            BigDecimal itemTotal = request.getItemTotalKrw() != null
                    ? request.getItemTotalKrw() : BigDecimal.ZERO;
            BigDecimal cifAmount = itemTotal.add(shippingFee).add(insuranceFee);

            // 6. 관세 = CIF × 평균 관세율 (HsCode에서 가져와야 하지만, 예상치로 request에서 받음)
            BigDecimal dutyRate = request.getAvgDutyRate() != null
                    ? request.getAvgDutyRate() : new BigDecimal("0.13");
            BigDecimal dutyAmount = cifAmount.multiply(dutyRate)
                    .setScale(0, RoundingMode.HALF_UP);

            // 7. 부가세 = (CIF + 관세) × 10%
            BigDecimal vatAmount = cifAmount.add(dutyAmount)
                    .multiply(new BigDecimal("0.10"))
                    .setScale(0, RoundingMode.HALF_UP);

            // 운임 합계 = 해외운임 + 항만/통관 + 보험 + 관세 + 부가세
            BigDecimal logisticsTotal = shippingFee
                    .add(portCustomsTotal)
                    .add(insuranceFee)
                    .add(dutyAmount)
                    .add(vatAmount);

            // 8. 구매 대행 수수료: Member → Buyer → FeePolicy
            BigDecimal serviceFee = BigDecimal.ZERO;
            BigDecimal docFee = BigDecimal.ZERO;
            String buyerGrade = "STANDARD";

            if (userDetails != null) {
                try {
                    Member member = memberRepository.findByMemLgnId(userDetails.getUsername())
                            .orElse(null);
                    if (member != null && member.getMemBizNo() != null) {
                        Buyer buyer = buyerService.findByBizNo(member.getMemBizNo());
                        buyerGrade = buyer.getBgpGr();
                    }
                } catch (Exception ignored) {}
            }

            try {
                FeePolicy commissionPolicy = feePolicyService
                        .findAllActiveByGrade(buyerGrade).stream()
                        .filter(fp -> "SERVICE_COMMISSION".equals(fp.getFpFeeTy()))
                        .filter(FeePolicy::isValid)
                        .findFirst().orElse(null);
                if (commissionPolicy != null && request.getItemTotalKrw() != null) {
                    serviceFee = commissionPolicy.apply(request.getItemTotalKrw());
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
                }
            } catch (Exception ignored) {}

            BigDecimal procurementTotal = serviceFee.add(docFee);

            return ResponseEntity.ok(EstimateFeeResponse.builder()
                    // 운임 상세
                    .factoryName(factoryName)
                    .factoryCity(factoryCity)
                    .countryCode(countryCode)
                    .transportType(transportTypeName)
                    .sizeType(sizeType)
                    .shippingFee(shippingFee)
                    .portFee(portFee)
                    .customsFee(customsFee)
                    .hsCodeFee(hsCodeFee)
                    .insuranceFee(insuranceFee)
                    .cifAmount(cifAmount)
                    .dutyRate(dutyRate)
                    .dutyAmount(dutyAmount)
                    .vatAmount(vatAmount)
                    .logisticsTotal(logisticsTotal)
                    // 대행 수수료
                    .buyerGrade(buyerGrade)
                    .serviceFee(serviceFee)
                    .docFee(docFee)
                    .inspectionFee(BigDecimal.ZERO)
                    .procurementTotal(procurementTotal)
                    .build());

        } catch (Exception e) {
            return ResponseEntity.ok(EstimateFeeResponse.empty());
        }
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
        private String regionName;
        private BigDecimal baseFee;
        private BigDecimal extraFee;
        private BigDecimal totalFee;

        public static DeliveryFeeResponse empty() {
            return DeliveryFeeResponse.builder()
                    .regionName(null).baseFee(null)
                    .extraFee(null).totalFee(null).build();
        }
    }

    @Getter
    @NoArgsConstructor
    public static class DraftRequest {
        private Long quId;
        private List<DraftItem> items;
        private String memo;

        @Getter
        @NoArgsConstructor
        public static class DraftItem {
            private Long stId;
            private Integer qty;
            private Long unGId;
            private String unGNm;
            private Integer unGQn;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class EstimateFeeRequest {
        private List<Long> stIds;
        private int totalDozen;
        private String region;
        private int shipmentCount;
        private BigDecimal itemTotalKrw;
        private BigDecimal avgDutyRate;
    }

    @Getter
    @Builder
    public static class EstimateFeeResponse {
        // 운송 상세
        private String factoryName;
        private String factoryCity;
        private String countryCode;
        private String transportType;
        private String sizeType;
        // 운임
        private BigDecimal shippingFee;
        private BigDecimal portFee;
        private BigDecimal customsFee;
        private BigDecimal hsCodeFee;
        private BigDecimal insuranceFee;
        private BigDecimal cifAmount;
        private BigDecimal dutyRate;
        private BigDecimal dutyAmount;
        private BigDecimal vatAmount;
        private BigDecimal logisticsTotal;
        // 대행 수수료
        private String buyerGrade;
        private BigDecimal serviceFee;
        private BigDecimal docFee;
        private BigDecimal inspectionFee;
        private BigDecimal procurementTotal;

        public static EstimateFeeResponse empty() {
            return EstimateFeeResponse.builder()
                    .shippingFee(BigDecimal.ZERO).portFee(BigDecimal.ZERO)
                    .customsFee(BigDecimal.ZERO).hsCodeFee(BigDecimal.ZERO)
                    .insuranceFee(BigDecimal.ZERO).cifAmount(BigDecimal.ZERO)
                    .dutyRate(BigDecimal.ZERO).dutyAmount(BigDecimal.ZERO)
                    .vatAmount(BigDecimal.ZERO).logisticsTotal(BigDecimal.ZERO)
                    .buyerGrade(null).serviceFee(BigDecimal.ZERO)
                    .docFee(BigDecimal.ZERO).inspectionFee(BigDecimal.ZERO)
                    .procurementTotal(BigDecimal.ZERO).build();
        }
    }
}
