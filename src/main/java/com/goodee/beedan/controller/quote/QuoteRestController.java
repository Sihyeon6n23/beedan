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
import com.goodee.beedan.service.quote.QuoteSubmitCheckService;
import com.goodee.beedan.service.quote.ShippingRateService;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
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
    private final QuoteSubmitCheckService quoteSubmitCheckService;
    private final com.goodee.beedan.service.exchangeRate.ExchangeRateService exchangeRateService;
    private final com.goodee.beedan.repository.buyer.BuyerGradePolicyRepository buyerGradePolicyRepository;
    private final com.goodee.beedan.repository.quote.QuoteShipFeeRepository quoteShipFeeRepository;
    private final com.goodee.beedan.repository.quote.ShippingInsuranceRepository shippingInsuranceRepository;

    private static final Map<String, String> REGION_NAMES = Map.of(
            "SEOUL", "서울특별시",
            "GYEONGGI", "경기도",
            "METRO", "수도권 (인천·세종·대전)",
            "PROVINCE", "지방"
    );

    // ── 사용자 견적 열람 처리 ─────────────────────────────────
    @PostMapping("/{quId}/opened")
    public ResponseEntity<Void> markOpened(@PathVariable Long quId) {
        quoteBaseService.userOpen(quId);
        return ResponseEntity.ok().build();
    }

    // ── 운영자 견적 열람 처리 ─────────────────────────────────
    @PostMapping("/{quId}/admin-opened")
    public ResponseEntity<Void> markAdminOpened(@PathVariable Long quId) {
        quoteBaseService.adminOpen(quId);
        return ResponseEntity.ok().build();
    }

    // ── 견적 승인 ─────────────────────────────────
    @PostMapping("/{quId}/approve")
    public ResponseEntity<Void> approveQuote(@PathVariable Long quId) {
        quoteBaseService.approve(quId);
        return ResponseEntity.ok().build();
    }

    // ── 견적 거절 ─────────────────────────────────
    @PostMapping("/{quId}/reject")
    public ResponseEntity<Void> rejectQuote(@PathVariable Long quId,
                                            @RequestBody Map<String, String> body) {
        quoteBaseService.reject(quId, body.get("reason"));
        return ResponseEntity.ok().build();
    }

    // ── 공급처(공장) 등록 (관리자) ─────────────────────
    @PostMapping("/factory")
    public ResponseEntity<Map<String, Object>> registerFactory(
            @RequestBody List<FactoryRegisterRequest> requests) {
        try {
            int created = 0;
            for (FactoryRegisterRequest req : requests) {
                if (req.getStIds() == null || req.getStIds().isEmpty()) continue;
                if (req.getCountryCode() == null || req.getCountryCode().isEmpty()) continue;

                // stId → brId 추출 (중복 제거)
                Set<Long> brIds = new HashSet<>();
                for (Long stId : req.getStIds()) {
                    Stock stock = stockRepository.findById(stId).orElse(null);
                    if (stock != null && stock.getBrId() != null) {
                        brIds.add(stock.getBrId());
                    }
                }

                for (Long brId : brIds) {
                    // 해당 브랜드에 이미 활성 공장이 있으면 스킵
                    List<Factory> existing = factoryRepository.findAllByBrIdAndFaYnTrue(brId);
                    if (!existing.isEmpty()) continue;

                    Factory factory = Factory.builder()
                            .brId(brId)
                            .faNm(req.getName())
                            .faCCd(req.getCountryCode())
                            .faCty(req.getCity())
                            .faYn(true)
                            .build();
                    factoryRepository.save(factory);
                    created++;
                }
            }
            return ResponseEntity.ok(Map.of("status", "ok", "created", created));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @Getter
    @NoArgsConstructor
    public static class FactoryRegisterRequest {
        private List<Long> stIds;  // 해당 브랜드의 상품 ID 목록
        private String name;       // 공장명
        private String countryCode; // 국가 코드
        private String city;       // 도시
    }

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

    // ── 제출 체크 항목 조회 ────────────────────────────
    @GetMapping("/submit-checks")
    public ResponseEntity<List<QuoteSubmitCheck>> getSubmitChecks() {
        return ResponseEntity.ok(quoteSubmitCheckService.findAllActive());
    }

    // ── 견적 제출 ─────────────────────────────────────
    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitQuote(
            @RequestBody SubmitRequest request) {
        try {
            // 재작성인 경우 기존 견적 거절 처리
            if (request.getFromQuId() != null) {
                String reason = request.getRejectReason() != null && !request.getRejectReason().isEmpty()
                        ? request.getRejectReason() : "견적 거절 후 재작성";
                quoteBaseService.reject(request.getFromQuId(), reason);
            }

            quoteBaseService.submit(request.getQuId());
            if (request.getChecks() != null && !request.getChecks().isEmpty()) {
                quoteSubmitCheckService.saveCheckLog(request.getQuId(), request.getChecks());
            }
            return ResponseEntity.ok(Map.of(
                    "status", "ok",
                    "redirectUrl", "/quote/detail?quId=" + request.getQuId()
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "error", "message", "제출 실패: " + e.getMessage()));
        }
    }

    @Getter
    @NoArgsConstructor
    public static class SubmitRequest {
        private Long quId;
        private Long fromQuId; // 재작성 시 원본 견적 ID (거절 처리 대상)
        private String rejectReason; // 재작성 시 거절 사유
        private Map<Long, Boolean> checks; // key: qscId, value: 동의 여부
    }

    // ── 임시저장 ─────────────────────────────────────
    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/draft")
    public ResponseEntity<Map<String, Object>> saveDraft(
            @RequestBody DraftRequest request) {

        try {
            QuoteBase quoteBase = quoteBaseService.findById(request.getQuId());
            if (!quoteBase.isEditable()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("status", "error", "message", "수정 불가 상태입니다."));
            }

            // 임시저장 시 상태가 null이면 TEMP_SAVE로 설정
            if (quoteBase.getQuStt() == null) {
                quoteBase.tempSave();
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
            quoteInfo.updateDraft(request.getMemo(), request.getSiId(), request.getStiId(), request.getGrandTotal());

            log.info("Draft save - grandTotal: {}, feeInfo null?: {}", request.getGrandTotal(), request.getFeeInfo() == null);

            // 비용 정보 저장
            if (request.getFeeInfo() != null) {
                log.info("Draft save - feeInfo: serviceFee={}, totalTax={}, totalShipFee={}, buyerGrade={}",
                        request.getFeeInfo().getServiceFee(), request.getFeeInfo().getTotalTax(),
                        request.getFeeInfo().getTotalShipFee(), request.getFeeInfo().getBuyerGrade());
                var fee = request.getFeeInfo();

                // 환율 조회
                var latestRates = exchangeRateService.findAllLatest();
                if (!latestRates.isEmpty()) {
                    var rate = latestRates.get(0);
                    quoteInfo.updateExchangeRate(rate.getErCr(), rate.getErRa());
                }

                // 등급 → bgpId, fpId 조회
                Long bgpId = null;
                Long fpId = null;
                String grade = fee.getBuyerGrade() != null ? fee.getBuyerGrade() : "STANDARD";
                try {
                    var bgp = buyerGradePolicyRepository.findByBgpGrAndBgpAcYnTrue(grade).orElse(null);
                    if (bgp != null) bgpId = bgp.getBgpId();
                } catch (Exception ignored) {}
                try {
                    var policies = feePolicyService.findAllActiveByGrade(grade);
                    var commPolicy = policies.stream()
                            .filter(fp -> "SERVICE_COMMISSION".equals(fp.getFpFeeTy()))
                            .findFirst().orElse(null);
                    if (commPolicy != null) fpId = commPolicy.getFpId();
                } catch (Exception ignored) {}

                quoteInfo.updateFeeInfo(
                        fee.getServiceFee(),
                        fee.getServiceFeeRate(),
                        fee.getServiceFeeAmount(),
                        fee.getDomesticFee(),
                        fee.getDomesticExtraFee(),
                        fee.getIntShipFee(),
                        fee.getDomShipFee(),
                        fee.getTotalShipFee(),
                        fee.getTotalTax(),
                        fee.getDiscountedTotal(),
                        bgpId,
                        fpId
                );
            }

            quoteInfo = quoteInfoRepository.save(quoteInfo);

            // 2. QuoteDetail merge (기존 항목 유지, 수정/추가/삭제)
            List<QuoteDetail> existingDetails = quoteDetailService.findAllByQuote(request.getQuId());
            Set<Long> processedDetailIds = new HashSet<>();

            // 보험 여부 + 보험율
            boolean insured = request.getSiId() != null;
            BigDecimal insuranceRate = BigDecimal.ZERO;
            if (insured) {
                try {
                    var si = shippingInsuranceRepository.findById(request.getSiId()).orElse(null);
                    if (si != null && si.getSiAm() != null) {
                        insuranceRate = si.getSiAm();
                    }
                } catch (Exception ignored) {
                    insuranceRate = new BigDecimal("0.005");
                }
            }

            // 배송비 할인율
            BigDecimal shippingDiscountRate = BigDecimal.ZERO;
            if (request.getFeeInfo() != null && request.getFeeInfo().getShippingDiscountRate() != null) {
                shippingDiscountRate = request.getFeeInfo().getShippingDiscountRate();
            }

            // 2-1. QuoteDetail 저장 + 공장별 그룹핑 준비
            Map<Long, Factory> factoryMap = new LinkedHashMap<>();
            Map<Long, Integer> factoryDozenMap = new LinkedHashMap<>();
            Map<Long, BigDecimal> factorySupplyMap = new LinkedHashMap<>();
            // 품목별 (공급가, 관세율) 리스트 — 안분 계산용
            Map<Long, List<BigDecimal[]>> factoryItemDetails = new LinkedHashMap<>();

            for (DraftRequest.DraftItem item : request.getItems()) {
                Stock stock = stockRepository.findById(item.getStId()).orElse(null);
                if (stock == null) continue;

                // 공장 조회
                Factory factory = null;
                if (stock.getBrId() != null) {
                    List<Factory> factories = factoryRepository.findAllByBrIdAndFaYnTrue(stock.getBrId());
                    factory = factories.isEmpty() ? null : factories.get(0);
                }

                // QuoteDetail: 기존 항목 매칭 (stId + grp) → 있으면 update, 없으면 insert
                QuoteDetail detail = existingDetails.stream()
                        .filter(d -> d.getStId().equals(item.getStId())
                                && Objects.equals(d.getQuDtGrp(), item.getGrp())
                                && !processedDetailIds.contains(d.getQuDtId()))
                        .findFirst()
                        .orElse(null);

                if (detail != null) {
                    detail.updateFrom(
                            item.getQty(), stock.getStNm(),
                            factory != null ? factory.getFaId() : null,
                            factory != null ? factory.getFaNm() : null,
                            item.getUnGId(), item.getUnGNm(), item.getUnGQn(),
                            stock.getStPr(), item.getSubtotalKrw(),
                            item.getRcId(),
                            item.getRcRgn(), item.getRcNm(), item.getRcAdr(),
                            item.getRcPhn(), item.getRcMemo()
                    );
                    processedDetailIds.add(detail.getQuDtId());
                } else {
                    detail = QuoteDetail.builder()
                            .quoteInfoId(quoteInfo.getQuInfoId())
                            .quoteId(request.getQuId())
                            .negoId(quoteBase.getNgId())
                            .stockId(item.getStId())
                            .stockQuantity(item.getQty())
                            .stockName(stock.getStNm())
                            .factoryId(factory != null ? factory.getFaId() : null)
                            .factoryName(factory != null ? factory.getFaNm() : null)
                            .unitGroupId(item.getUnGId())
                            .unitGroupName(item.getUnGNm())
                            .unitGroupQuantity(item.getUnGQn())
                            .foreignPrice(stock.getStPr())
                            .krwTotal(item.getSubtotalKrw())
                            .receiverId(item.getRcId())
                            .group(item.getGrp())
                            .rcRegion(item.getRcRgn())
                            .rcName(item.getRcNm())
                            .rcAddress(item.getRcAdr())
                            .rcPhone(item.getRcPhn())
                            .rcMemo(item.getRcMemo())
                            .build();
                }
                quoteDetailService.save(detail);

                // 공장별 그룹핑 데이터 누적
                Long faKey = factory != null ? factory.getFaId() : -1L;
                if (factory != null) factoryMap.putIfAbsent(faKey, factory);

                // 다스 수 합산
                int dozen = 0;
                if (item.getUnGQn() != null && item.getUnGQn() > 0 && item.getQty() != null) {
                    dozen = (int) Math.ceil((double) item.getQty() / item.getUnGQn());
                }
                factoryDozenMap.merge(faKey, dozen, Integer::sum);

                // 공급가 합산
                BigDecimal itemKrw = item.getSubtotalKrw() != null ? item.getSubtotalKrw() : BigDecimal.ZERO;
                factorySupplyMap.merge(faKey, itemKrw, BigDecimal::add);

                // 품목별 관세율 + 공급가 보관 (안분 계산용)
                BigDecimal dutyRate = new BigDecimal("0.13");
                try {
                    if (stock.getCatId() != null) {
                        var hsCode = hsCodeRepository.findByCatId(stock.getCatId()).orElse(null);
                        if (hsCode != null && hsCode.getHsDuRa() != null) {
                            dutyRate = hsCode.getHsDuRa();
                        }
                    }
                } catch (Exception ignored) {}
                factoryItemDetails.computeIfAbsent(faKey, k -> new ArrayList<>())
                        .add(new BigDecimal[]{ itemKrw, dutyRate });
            }

            // 요청에 없는 기존 QuoteDetail 삭제
            existingDetails.stream()
                    .filter(d -> !processedDetailIds.contains(d.getQuDtId()))
                    .forEach(d -> quoteDetailService.delete(d.getQuDtId()));

            // 2-2. QuoteShipFee: 공장별 그룹 단위로 생성 (estimate-fees와 동일)
            List<QuoteShipFee> existingShipFees = quoteShipFeeRepository.findAllByQuId(request.getQuId());
            Set<Long> processedShipFeeIds = new HashSet<>();

            // admin 수동 오버라이드 맵 (factoryIndex → ManualShipFee)
            Map<Integer, DraftRequest.ManualShipFee> manualMap = new HashMap<>();
            if (request.getManualShipFees() != null) {
                for (DraftRequest.ManualShipFee msf : request.getManualShipFees()) {
                    if (msf.getFactoryIndex() != null) manualMap.put(msf.getFactoryIndex(), msf);
                }
            }

            int factoryIdx = 0;
            for (Map.Entry<Long, Integer> entry : factoryDozenMap.entrySet()) {
                Long faKey = entry.getKey();
                int groupDozen = entry.getValue();
                BigDecimal groupSupply = factorySupplyMap.getOrDefault(faKey, BigDecimal.ZERO);
                Factory factory = factoryMap.get(faKey);
                Long faId = faKey == -1L ? null : faKey;
                String countryCode = factory != null ? factory.getFaCCd() : null;

                // 품목별 안분 데이터
                List<BigDecimal[]> itemDetails = factoryItemDetails.getOrDefault(faKey, Collections.emptyList());

                // 기존 ShipFee 매칭 (faId)
                QuoteShipFee shipFee = existingShipFees.stream()
                        .filter(sf -> Objects.equals(sf.getFaId(), faId)
                                && !processedShipFeeIds.contains(sf.getQsfId()))
                        .findFirst()
                        .orElse(null);

                if (shipFee != null) {
                    shipFee.resetForRecalculation(
                            factory != null ? factory.getFaNm() : null,
                            countryCode, "SEA", groupDozen
                    );
                    processedShipFeeIds.add(shipFee.getQsfId());
                } else {
                    shipFee = QuoteShipFee.builder()
                            .quoteInfoId(quoteInfo.getQuInfoId())
                            .quoteId(request.getQuId())
                            .negoId(quoteBase.getNgId())
                            .factoryId(faId)
                            .factoryName(factory != null ? factory.getFaNm() : null)
                            .factoryCountryCode(countryCode)
                            .transportType("SEA")
                            .totalDozen(groupDozen)
                            .build();
                }

                // 해외 운임
                BigDecimal shippingFee = BigDecimal.ZERO;
                String sizeType = "SMALL";
                try {
                    if (countryCode != null) {
                        var sr = shippingRateService.findByCCdAndTransportType(
                                countryCode, TransportType.SEA);
                        shippingFee = sr.getApplicableAmount(groupDozen);
                        sizeType = sr.getSizeType(groupDozen);
                    }
                } catch (Exception ignored) {}
                // admin 수동 오버라이드 적용
                DraftRequest.ManualShipFee manual = manualMap.get(factoryIdx);
                if (manual != null) {
                    if (manual.getShippingFee() != null) shippingFee = manual.getShippingFee();
                    shipFee.overrideShippingFee(shippingFee, "관리자 수동 입력");
                } else {
                    shipFee.setShippingFee(shippingFee);
                }

                // 항만/통관/HS
                BigDecimal portFee = BigDecimal.ZERO, customsFee = BigDecimal.ZERO, hsCodeFee = BigDecimal.ZERO;
                try { portFee = portCustomsRateService.findActiveByType("PORT").getApplicableAmount(sizeType); } catch (Exception ignored) {}
                try { customsFee = portCustomsRateService.findActiveByType("CUSTOMS").getApplicableAmount(sizeType); } catch (Exception ignored) {}
                try { hsCodeFee = portCustomsRateService.findActiveByType("HS_CODE").getApplicableAmount(sizeType); } catch (Exception ignored) {}
                if (manual != null) {
                    if (manual.getPortFee() != null) portFee = manual.getPortFee();
                    if (manual.getCustomsFee() != null) customsFee = manual.getCustomsFee();
                    if (manual.getHsCodeFee() != null) hsCodeFee = manual.getHsCodeFee();
                }
                shipFee.setPortCustomsFee(portFee, customsFee, hsCodeFee);

                // 보험 (그룹 공급가 기준)
                if (insured && groupSupply.compareTo(BigDecimal.ZERO) > 0) {
                    shipFee.setInsurance(groupSupply.multiply(insuranceRate).setScale(0, RoundingMode.HALF_UP));
                }

                // CIF (그룹 공급가 기준)
                shipFee.calculateCif(groupSupply);

                // 관세/부가세: 품목별 운임·보험 안분 후 개별 CIF 기준으로 계산
                BigDecimal grpDutyTotal = BigDecimal.ZERO;
                BigDecimal grpVatTotal = BigDecimal.ZERO;
                BigDecimal grpInsAm = shipFee.getQsfInsAm() != null ? shipFee.getQsfInsAm() : BigDecimal.ZERO;
                BigDecimal representativeDutyRate = new BigDecimal("0.13");

                for (BigDecimal[] detail : itemDetails) {
                    BigDecimal itemSupply = detail[0];
                    BigDecimal itemDutyRate = detail[1];
                    representativeDutyRate = itemDutyRate; // 마지막 품목의 관세율 (표시용)

                    BigDecimal ratio = groupSupply.compareTo(BigDecimal.ZERO) > 0
                            ? itemSupply.divide(groupSupply, 10, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    BigDecimal itemShipping = shippingFee.multiply(ratio).setScale(0, RoundingMode.HALF_UP);
                    BigDecimal itemIns = grpInsAm.multiply(ratio).setScale(0, RoundingMode.HALF_UP);

                    BigDecimal itemCif = itemSupply.add(itemShipping).add(itemIns);
                    BigDecimal itemDuty = itemCif.multiply(itemDutyRate).setScale(0, RoundingMode.HALF_UP);
                    BigDecimal itemVat = itemCif.add(itemDuty).multiply(new BigDecimal("0.10")).setScale(0, RoundingMode.HALF_UP);

                    grpDutyTotal = grpDutyTotal.add(itemDuty);
                    grpVatTotal = grpVatTotal.add(itemVat);
                }
                shipFee.setDutyAndVat(representativeDutyRate, grpDutyTotal, grpVatTotal);

                // 할인율 적용 + 합계
                shipFee.applyDiscount(shippingDiscountRate);
                shipFee.calculateTotal();

                quoteShipFeeRepository.save(shipFee);
                factoryIdx++;
            }

            // 요청에 없는 기존 QuoteShipFee 삭제
            existingShipFees.stream()
                    .filter(sf -> !processedShipFeeIds.contains(sf.getQsfId()))
                    .forEach(sf -> quoteShipFeeRepository.delete(sf));

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
                String factoryName = factory != null ? factory.getFaNm() : "알 수 없음 (큐레이터가 공급처 확인 후 재안내드립니다)";
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

                // (avgDutyRate는 아래 품목별 안분에서 개별 적용)

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

                // 보험 (그룹 전체)
                BigDecimal grpInsurance = BigDecimal.ZERO;
                if (insured && groupSupply.compareTo(BigDecimal.ZERO) > 0) {
                    grpInsurance = groupSupply.multiply(insuranceRate).setScale(0, RoundingMode.HALF_UP);
                }

                // 관세/부가세: 품목별 운임·보험 안분 후 개별 CIF 기준으로 계산
                BigDecimal grpCif = BigDecimal.ZERO;
                BigDecimal grpDuty = BigDecimal.ZERO;
                BigDecimal grpVat = BigDecimal.ZERO;

                for (EstimateItem gi : groupItems) {
                    BigDecimal itemSupply = gi.getSupplyKrw() != null ? gi.getSupplyKrw() : BigDecimal.ZERO;
                    // 상품가액 비율로 운임·보험 안분
                    BigDecimal ratio = groupSupply.compareTo(BigDecimal.ZERO) > 0
                            ? itemSupply.divide(groupSupply, 10, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    BigDecimal itemShipping = grpShipping.multiply(ratio).setScale(0, RoundingMode.HALF_UP);
                    BigDecimal itemInsurance = grpInsurance.multiply(ratio).setScale(0, RoundingMode.HALF_UP);

                    BigDecimal itemCif = itemSupply.add(itemShipping).add(itemInsurance);
                    grpCif = grpCif.add(itemCif);

                    BigDecimal itemDutyRate = (gi.getDutyRate() != null && gi.getDutyRate().compareTo(BigDecimal.ZERO) > 0)
                            ? gi.getDutyRate() : new BigDecimal("0.13");
                    BigDecimal itemDuty = itemCif.multiply(itemDutyRate).setScale(0, RoundingMode.HALF_UP);
                    BigDecimal itemVat = itemCif.add(itemDuty).multiply(new BigDecimal("0.10")).setScale(0, RoundingMode.HALF_UP);

                    grpDuty = grpDuty.add(itemDuty);
                    grpVat = grpVat.add(itemVat);
                }

                // 대표 관세율 (표시용, 실제 계산은 품목별)
                BigDecimal avgDutyRateDisplay = dutyRateCount > 0
                        ? dutyRateSum.divide(BigDecimal.valueOf(dutyRateCount), 4, RoundingMode.HALF_UP)
                        : new BigDecimal("0.13");

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
                        .dutyRate(avgDutyRateDisplay)
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
        private BigDecimal grandTotal; // 최종 금액
        private DraftFeeInfo feeInfo;  // 비용 정보
        private List<ManualShipFee> manualShipFees; // admin 수동 운임 오버라이드

        @Getter
        @NoArgsConstructor
        public static class ManualShipFee {
            private Integer factoryIndex;
            private BigDecimal shippingFee;
            private BigDecimal portFee;
            private BigDecimal customsFee;
            private BigDecimal hsCodeFee;
        }

        @Getter
        @NoArgsConstructor
        public static class DraftFeeInfo {
            private BigDecimal serviceFee;         // 대행 수수료
            private BigDecimal serviceFeeRate;     // 할인율
            private BigDecimal serviceFeeAmount;   // 할인 후 수수료
            private BigDecimal domesticFee;        // 국내 배송비
            private BigDecimal domesticExtraFee;   // 도서산간 추가
            private BigDecimal intShipFee;         // 국제 배송비 합계
            private BigDecimal domShipFee;         // 국내 배송비 합계
            private BigDecimal totalShipFee;       // 전체 배송비 합계
            private BigDecimal totalTax;           // 총 세액
            private BigDecimal discountedTotal;    // 할인 적용 총액
            private String buyerGrade;             // 적용 등급
            private BigDecimal shippingDiscountRate; // 운송 할인율
        }

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
            // 분할배송
            private Integer grp;             // 그룹 인덱스 (행 번호)
            private String rcRgn;            // 배송 지역
            private String rcNm;             // 수령인명
            private String rcAdr;            // 수령지 주소
            private String rcPhn;            // 수령인 연락처
            private String rcMemo;           // 배달 요청사항
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
