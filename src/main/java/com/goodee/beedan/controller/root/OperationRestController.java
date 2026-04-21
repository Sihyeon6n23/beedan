package com.goodee.beedan.controller.root;

import com.goodee.beedan.entity.*;
import com.goodee.beedan.repository.buyer.BuyerGradePolicyRepository;
import com.goodee.beedan.repository.quote.*;
import com.goodee.beedan.service.quote.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/root/operation")
@RequiredArgsConstructor
public class OperationRestController {

    private final BuyerGradePolicyRepository buyerGradePolicyRepository;
    private final ShippingInsuranceRepository shippingInsuranceRepository;
    private final StockInspectionRepository stockInspectionRepository;
    private final com.goodee.beedan.repository.quote.UnitGroupRepository unitGroupRepository;
    private final com.goodee.beedan.repository.quote.UnitDiscountRepository unitDiscountRepo;
    private final com.goodee.beedan.repository.quote.ShippingRateRepository shippingRateRepository;
    private final com.goodee.beedan.repository.quote.PortCustomsRateRepository portCustomsRateRepository;
    private final com.goodee.beedan.repository.quote.DomesticDeliveryRateRepository domesticDeliveryRateRepository;
    private final QuoteSubmitCheckRepository quoteSubmitCheckRepository;
    private final com.goodee.beedan.repository.buyer.FeePolicyRepository feePolicyRepository;

    // 중복 이름 등 명시적 오류를 프론트에 전달하기 위한 공통 에러 응답 헬퍼
    private ResponseEntity<?> fail(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("status", "error", "message", message));
    }

    // ========== 1. 고객사 등급 정책 ==========

    @GetMapping("/grade/{id}")
    public ResponseEntity<?> getGrade(@PathVariable Long id) {
        return ResponseEntity.ok(buyerGradePolicyRepository.findById(id).orElseThrow());
    }

    @PostMapping("/grade")
    public ResponseEntity<?> createGrade(@RequestBody GradeRequest req) {
        BuyerGradePolicy grade = BuyerGradePolicy.builder()
                .bgpGr(req.getBgpGr())
                .bgpMinOrdCnt(req.getBgpMinOrdCnt())
                .bgpMinTtAm(req.getBgpMinTtAm())
                .bgpEfFrDt(toStartOfDay(req.getBgpEfFrDt()))
                .bgpEfToDt(toStartOfDay(req.getBgpEfToDt()))
                .bgpDes(req.getBgpDes())
                .bgpAcYn(true)
                .build();
        buyerGradePolicyRepository.save(grade);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PutMapping("/grade/{id}")
    public ResponseEntity<?> updateGrade(@PathVariable Long id, @RequestBody GradeRequest req) {
        BuyerGradePolicy grade = buyerGradePolicyRepository.findById(id).orElseThrow();
        grade.update(req.getBgpMinOrdCnt(), req.getBgpMinTtAm(),
                toStartOfDay(req.getBgpEfFrDt()), toStartOfDay(req.getBgpEfToDt()),
                req.getBgpDes());
        buyerGradePolicyRepository.save(grade);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    private static LocalDateTime toStartOfDay(LocalDate d) {
        return d != null ? d.atStartOfDay() : null;
    }

    @DeleteMapping("/grade/{id}")
    public ResponseEntity<?> deleteGrade(@PathVariable Long id) {
        BuyerGradePolicy grade = buyerGradePolicyRepository.findById(id).orElseThrow();
        grade.deactivate();
        buyerGradePolicyRepository.save(grade);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    // ========== 2. 적하보험 ==========

    @GetMapping("/insurance/{id}")
    public ResponseEntity<?> getInsurance(@PathVariable Long id) {
        return ResponseEntity.ok(shippingInsuranceRepository.findById(id).orElseThrow());
    }

    @PostMapping("/insurance")
    public ResponseEntity<?> createInsurance(@RequestBody InsuranceRequest req) {
        ShippingInsurance ins = ShippingInsurance.builder()
                .name(req.getSiNm()).amount(req.getSiAm()).description(req.getSiDes()).build();
        shippingInsuranceRepository.save(ins);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PutMapping("/insurance/{id}")
    public ResponseEntity<?> updateInsurance(@PathVariable Long id, @RequestBody InsuranceRequest req) {
        ShippingInsurance ins = shippingInsuranceRepository.findById(id).orElseThrow();
        ins.update(req.getSiNm(), req.getSiAm(), req.getSiDes());
        shippingInsuranceRepository.save(ins);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @DeleteMapping("/insurance/{id}")
    public ResponseEntity<?> deleteInsurance(@PathVariable Long id) {
        ShippingInsurance ins = shippingInsuranceRepository.findById(id).orElseThrow();
        ins.deactivate();
        shippingInsuranceRepository.save(ins);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    // ========== 3. 품질검사 ==========

    @GetMapping("/inspection/{id}")
    public ResponseEntity<?> getInspection(@PathVariable Long id) {
        return ResponseEntity.ok(stockInspectionRepository.findById(id).orElseThrow());
    }

    @PostMapping("/inspection")
    public ResponseEntity<?> createInspection(@RequestBody InspectionRequest req) {
        StockInspection insp = StockInspection.builder()
                .name(req.getStiNm()).amount(req.getStiAm()).description(req.getStiDes()).build();
        stockInspectionRepository.save(insp);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PutMapping("/inspection/{id}")
    public ResponseEntity<?> updateInspection(@PathVariable Long id, @RequestBody InspectionRequest req) {
        StockInspection insp = stockInspectionRepository.findById(id).orElseThrow();
        insp.update(req.getStiNm(), req.getStiAm(), req.getStiDes());
        stockInspectionRepository.save(insp);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @DeleteMapping("/inspection/{id}")
    public ResponseEntity<?> deleteInspection(@PathVariable Long id) {
        StockInspection insp = stockInspectionRepository.findById(id).orElseThrow();
        insp.deactivate();
        stockInspectionRepository.save(insp);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    // ========== 4. 묶음 단위 ==========

    @GetMapping("/unit-group/{id}")
    public ResponseEntity<?> getUnitGroup(@PathVariable Long id) {
        return ResponseEntity.ok(unitGroupRepository.findById(id).orElseThrow());
    }

    @PostMapping("/unit-group")
    public ResponseEntity<?> createUnitGroup(@RequestBody UnitGroupRequest req) {
        if (req.getUnGNm() == null || req.getUnGNm().isBlank()) return fail("단위명을 입력해주세요.");
        if (req.getUnGQn() == null || req.getUnGQn() <= 0) return fail("단위당 수량은 1 이상이어야 합니다.");
        // 중복 이름(활성/비활성 모두 포함) 방지
        if (unitGroupRepository.existsByUnGNm(req.getUnGNm())) {
            return fail("이미 존재하는 단위명입니다: " + req.getUnGNm());
        }
        try {
            UnitGroup ug = UnitGroup.builder()
                    .unGNm(req.getUnGNm())
                    .unGQn(req.getUnGQn())
                    .unGYn(true)
                    .build();
            unitGroupRepository.save(ug);
            return ResponseEntity.ok(Map.of("status", "ok"));
        } catch (Exception e) {
            log.error("묶음 단위 생성 실패: {}", e.getMessage(), e);
            return fail("묶음 단위 생성 중 오류: " + e.getMessage());
        }
    }

    @PutMapping("/unit-group/{id}")
    public ResponseEntity<?> updateUnitGroup(@PathVariable Long id, @RequestBody UnitGroupRequest req) {
        UnitGroup ug = unitGroupRepository.findById(id)
                .orElse(null);
        if (ug == null) return fail("묶음 단위를 찾을 수 없습니다. id=" + id);
        if (req.getUnGNm() == null || req.getUnGNm().isBlank()) return fail("단위명을 입력해주세요.");
        if (req.getUnGQn() == null || req.getUnGQn() <= 0) return fail("단위당 수량은 1 이상이어야 합니다.");
        // 이름이 바뀐 경우에만 중복 검사 (본인 레코드 제외)
        if (!req.getUnGNm().equals(ug.getUnGNm())
                && unitGroupRepository.findByUnGNm(req.getUnGNm())
                    .filter(other -> !other.getUnGId().equals(id))
                    .isPresent()) {
            return fail("이미 존재하는 단위명입니다: " + req.getUnGNm());
        }
        try {
            ug.update(req.getUnGNm(), req.getUnGQn());
            unitGroupRepository.save(ug);
            return ResponseEntity.ok(Map.of("status", "ok"));
        } catch (Exception e) {
            log.error("묶음 단위 수정 실패: {}", e.getMessage(), e);
            return fail("묶음 단위 수정 중 오류: " + e.getMessage());
        }
    }

    @DeleteMapping("/unit-group/{id}")
    public ResponseEntity<?> deleteUnitGroup(@PathVariable Long id) {
        UnitGroup ug = unitGroupRepository.findById(id).orElse(null);
        if (ug == null) return fail("묶음 단위를 찾을 수 없습니다. id=" + id);
        try {
            ug.deactivate();
            unitGroupRepository.save(ug);
            return ResponseEntity.ok(Map.of("status", "ok"));
        } catch (Exception e) {
            log.error("묶음 단위 삭제 실패: {}", e.getMessage(), e);
            return fail("묶음 단위 삭제 중 오류: " + e.getMessage());
        }
    }

    // ========== 5. 해외 운임 ==========

    @GetMapping("/shipping-rate/{id}")
    public ResponseEntity<?> getShippingRate(@PathVariable Long id) {
        return ResponseEntity.ok(shippingRateRepository.findById(id).orElseThrow());
    }

    @PostMapping("/shipping-rate")
    public ResponseEntity<?> createShippingRate(@RequestBody ShippingRateRequest req) {
        ShippingRate sr = ShippingRate.builder()
                .countryCode(req.getSrCCd())
                .transportType(com.goodee.beedan.common.constant.TransportType.valueOf(req.getSrTrspTy()))
                .smallQuantity(req.getSrSmQn()).smallAmount(req.getSrSmAm())
                .mediumQuantity(req.getSrMdQn()).mediumAmount(req.getSrMdAm())
                .largeQuantity(req.getSrLgQn()).largeAmount(req.getSrLgAm())
                .description(req.getSrDes())
                .build();
        shippingRateRepository.save(sr);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PutMapping("/shipping-rate/{id}")
    public ResponseEntity<?> updateShippingRate(@PathVariable Long id, @RequestBody ShippingRateRequest req) {
        ShippingRate sr = shippingRateRepository.findById(id).orElseThrow();
        sr.update(req.getSrSmAm(), req.getSrMdAm(), req.getSrLgAm(), req.getSrDes());
        shippingRateRepository.save(sr);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @DeleteMapping("/shipping-rate/{id}")
    public ResponseEntity<?> deleteShippingRate(@PathVariable Long id) {
        ShippingRate sr = shippingRateRepository.findById(id).orElseThrow();
        sr.deactivate();
        shippingRateRepository.save(sr);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    // ========== 6. 항만/통관 비용 ==========

    @GetMapping("/port-customs/{id}")
    public ResponseEntity<?> getPortCustoms(@PathVariable Long id) {
        return ResponseEntity.ok(portCustomsRateRepository.findById(id).orElseThrow());
    }

    @PostMapping("/port-customs")
    public ResponseEntity<?> createPortCustoms(@RequestBody PortCustomsRequest req) {
        PortCustomsRate pcr = PortCustomsRate.builder()
                .type(req.getPcrTy())
                .smallAmount(req.getPcrSmAm()).mediumAmount(req.getPcrMdAm()).largeAmount(req.getPcrLgAm())
                .description(req.getPcrDes())
                .build();
        portCustomsRateRepository.save(pcr);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PutMapping("/port-customs/{id}")
    public ResponseEntity<?> updatePortCustoms(@PathVariable Long id, @RequestBody PortCustomsRequest req) {
        PortCustomsRate pcr = portCustomsRateRepository.findById(id).orElseThrow();
        pcr.update(req.getPcrSmAm(), req.getPcrMdAm(), req.getPcrLgAm(), req.getPcrDes());
        portCustomsRateRepository.save(pcr);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @DeleteMapping("/port-customs/{id}")
    public ResponseEntity<?> deletePortCustoms(@PathVariable Long id) {
        PortCustomsRate pcr = portCustomsRateRepository.findById(id).orElseThrow();
        pcr.deactivate();
        portCustomsRateRepository.save(pcr);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    // ========== 7. 국내 배송 요율 ==========

    @GetMapping("/delivery-rate/{id}")
    public ResponseEntity<?> getDeliveryRate(@PathVariable Long id) {
        return ResponseEntity.ok(domesticDeliveryRateRepository.findById(id).orElseThrow());
    }

    @PostMapping("/delivery-rate")
    public ResponseEntity<?> createDeliveryRate(@RequestBody DeliveryRateRequest req) {
        DomesticDeliveryRate ddr = DomesticDeliveryRate.builder()
                .region(req.getDdrRgn()).amount(req.getDdrAm()).extraAmount(req.getDdrEAm())
                .description(req.getDdrDes())
                .build();
        domesticDeliveryRateRepository.save(ddr);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PutMapping("/delivery-rate/{id}")
    public ResponseEntity<?> updateDeliveryRate(@PathVariable Long id, @RequestBody DeliveryRateRequest req) {
        DomesticDeliveryRate ddr = domesticDeliveryRateRepository.findById(id).orElseThrow();
        ddr.update(req.getDdrAm(), req.getDdrEAm(), req.getDdrDes());
        domesticDeliveryRateRepository.save(ddr);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @DeleteMapping("/delivery-rate/{id}")
    public ResponseEntity<?> deleteDeliveryRate(@PathVariable Long id) {
        DomesticDeliveryRate ddr = domesticDeliveryRateRepository.findById(id).orElseThrow();
        ddr.deactivate();
        domesticDeliveryRateRepository.save(ddr);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    // ========== 8. 견적 제출 체크리스트 ==========

    @GetMapping("/submit-check/{id}")
    public ResponseEntity<?> getSubmitCheck(@PathVariable Long id) {
        return ResponseEntity.ok(quoteSubmitCheckRepository.findById(id).orElseThrow());
    }

    @PostMapping("/submit-check")
    public ResponseEntity<?> createSubmitCheck(@RequestBody SubmitCheckRequest req) {
        // Front-end form only sends qscDes/qscSort — default the rest so the
        // record shows up in the list (which filters by qscRqYn=true).
        QuoteSubmitCheck qsc = QuoteSubmitCheck.builder()
                .qscRqYn(req.getQscRqYn() != null ? req.getQscRqYn() : true)
                .qscDes(req.getQscDes())
                .qscKey(req.getQscKey())
                .qscDfltYn(req.getQscDfltYn() != null ? req.getQscDfltYn() : false)
                .qscSort(req.getQscSort())
                .build();
        quoteSubmitCheckRepository.save(qsc);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PutMapping("/submit-check/{id}")
    public ResponseEntity<?> updateSubmitCheck(@PathVariable Long id, @RequestBody SubmitCheckRequest req) {
        QuoteSubmitCheck qsc = quoteSubmitCheckRepository.findById(id).orElseThrow();
        // Preserve existing Boolean/key values when the request omits them.
        qsc.update(
                req.getQscRqYn() != null ? req.getQscRqYn() : qsc.getQscRqYn(),
                req.getQscDes(),
                req.getQscKey() != null ? req.getQscKey() : qsc.getQscKey(),
                req.getQscDfltYn() != null ? req.getQscDfltYn() : qsc.getQscDfltYn(),
                req.getQscSort()
        );
        quoteSubmitCheckRepository.save(qsc);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @DeleteMapping("/submit-check/{id}")
    public ResponseEntity<?> deleteSubmitCheck(@PathVariable Long id) {
        QuoteSubmitCheck qsc = quoteSubmitCheckRepository.findById(id).orElseThrow();
        qsc.deactivate();
        quoteSubmitCheckRepository.save(qsc);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    // ========== 묶음 할인 정책 (UnitGroup 상세) ==========

    @GetMapping("/unit-discount/{id}")
    public ResponseEntity<?> getUnitDiscount(@PathVariable Long id) {
        UnitDiscount ud = unitDiscountRepo.findById(id).orElse(null);
        if (ud == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        return ResponseEntity.ok(ud);
    }

    @PostMapping("/unit-discount")
    public ResponseEntity<?> createUnitDiscount(@RequestBody UnitDiscountReq req) {
        if (req.getUnGId() == null) return fail("묶음 단위 ID가 필요합니다.");
        if (req.getUnDOvTy() == null || req.getUnDOvTy().isBlank())
            return fail("중복 처리 방식을 선택해주세요.");
        try {
            UnitDiscount ud = UnitDiscount.builder()
                    .unitGroupId(req.getUnGId())
                    .minQuantity(req.getUnDMinQn())
                    .minAmount(req.getUnDMinAm())
                    .quantityDiscountRate(req.getUnDQnDr())
                    .amountDiscountRate(req.getUnDAmDr())
                    .overlapType(com.goodee.beedan.common.constant.OverlapType.valueOf(req.getUnDOvTy()))
                    .description(req.getUnDDes())
                    .build();
            unitDiscountRepo.save(ud);
            return ResponseEntity.ok(Map.of("status", "ok"));
        } catch (Exception e) {
            log.error("묶음 할인 생성 실패: {}", e.getMessage(), e);
            return fail("생성 중 오류: " + e.getMessage());
        }
    }

    @PutMapping("/unit-discount/{id}")
    public ResponseEntity<?> updateUnitDiscount(@PathVariable Long id, @RequestBody UnitDiscountReq req) {
        UnitDiscount existing = unitDiscountRepo.findById(id).orElse(null);
        if (existing == null) return fail("묶음 할인 정책을 찾을 수 없습니다. id=" + id);
        if (req.getUnDOvTy() == null || req.getUnDOvTy().isBlank())
            return fail("중복 처리 방식을 선택해주세요.");
        try {
            // UnitDiscount 엔티티에는 update() 가 없으므로 delete+save 로 대체
            Long unGId = existing.getUnGId();
            unitDiscountRepo.delete(existing);
            UnitDiscount ud = UnitDiscount.builder()
                    .unitGroupId(unGId)
                    .minQuantity(req.getUnDMinQn())
                    .minAmount(req.getUnDMinAm())
                    .quantityDiscountRate(req.getUnDQnDr())
                    .amountDiscountRate(req.getUnDAmDr())
                    .overlapType(com.goodee.beedan.common.constant.OverlapType.valueOf(req.getUnDOvTy()))
                    .description(req.getUnDDes())
                    .build();
            unitDiscountRepo.save(ud);
            return ResponseEntity.ok(Map.of("status", "ok"));
        } catch (Exception e) {
            log.error("묶음 할인 수정 실패: {}", e.getMessage(), e);
            return fail("수정 중 오류: " + e.getMessage());
        }
    }

    @DeleteMapping("/unit-discount/{id}")
    public ResponseEntity<?> deleteUnitDiscount(@PathVariable Long id) {
        UnitDiscount existing = unitDiscountRepo.findById(id).orElse(null);
        if (existing == null) return fail("묶음 할인 정책을 찾을 수 없습니다. id=" + id);
        try {
            unitDiscountRepo.delete(existing);
            return ResponseEntity.ok(Map.of("status", "ok"));
        } catch (Exception e) {
            log.error("묶음 할인 삭제 실패: {}", e.getMessage(), e);
            return fail("삭제 중 오류: " + e.getMessage());
        }
    }

    // ========== 9. 수수료 정책 ==========

    @GetMapping("/fee-policy/{id}")
    public ResponseEntity<?> getFeePolicy(@PathVariable Long id) {
        return ResponseEntity.ok(feePolicyRepository.findById(id).orElseThrow());
    }

    @PostMapping("/fee-policy")
    public ResponseEntity<?> createFeePolicy(@RequestBody FeePolicyRequest req) {
        FeePolicy fp = FeePolicy.builder()
                .bgpGr(req.getBgpGr())
                .fpFeeTy(req.getFpFeeTy())
                .fpCalcTy(com.goodee.beedan.common.policy.FeeCalculationType.valueOf(req.getFpCalcTy()))
                .fpVal(req.getFpVal())
                .fpAcYn(true)
                .fpEfFrDt(toStartOfDay(req.getFpEfFrDt()))
                .fpEfToDt(toStartOfDay(req.getFpEfToDt()))
                .fpDes(req.getFpDes())
                .build();
        feePolicyRepository.save(fp);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PutMapping("/fee-policy/{id}")
    public ResponseEntity<?> updateFeePolicy(@PathVariable Long id, @RequestBody FeePolicyRequest req) {
        FeePolicy fp = feePolicyRepository.findById(id).orElseThrow();
        fp.update(req.getFpCalcTy(), req.getFpVal(),
                toStartOfDay(req.getFpEfFrDt()), toStartOfDay(req.getFpEfToDt()),
                req.getFpDes());
        feePolicyRepository.save(fp);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @DeleteMapping("/fee-policy/{id}")
    public ResponseEntity<?> deleteFeePolicy(@PathVariable Long id) {
        FeePolicy fp = feePolicyRepository.findById(id).orElseThrow();
        fp.deactivate();
        feePolicyRepository.save(fp);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    // ========== DTOs ==========

    @Getter @NoArgsConstructor
    public static class GradeRequest {
        private String bgpGr;
        private Integer bgpMinOrdCnt;
        private BigDecimal bgpMinTtAm;
        // Front-end <input type="date"> sends "yyyy-MM-dd" — accept as LocalDate
        private LocalDate bgpEfFrDt;
        private LocalDate bgpEfToDt;
        private String bgpDes;
    }

    @Getter @NoArgsConstructor
    public static class InsuranceRequest {
        private String siNm;
        private BigDecimal siAm;
        private String siDes;
    }

    @Getter @NoArgsConstructor
    public static class InspectionRequest {
        private String stiNm;
        private BigDecimal stiAm;
        private String stiDes;
    }

    @Getter @NoArgsConstructor
    public static class UnitGroupRequest {
        private String unGNm;
        private Integer unGQn;
    }

    @Getter @NoArgsConstructor
    public static class ShippingRateRequest {
        private String srCCd;
        private String srTrspTy;
        private Integer srSmQn;
        private BigDecimal srSmAm;
        private Integer srMdQn;
        private BigDecimal srMdAm;
        private Integer srLgQn;
        private BigDecimal srLgAm;
        private String srDes;
    }

    @Getter @NoArgsConstructor
    public static class PortCustomsRequest {
        private String pcrTy;
        private BigDecimal pcrSmAm;
        private BigDecimal pcrMdAm;
        private BigDecimal pcrLgAm;
        private String pcrDes;
    }

    @Getter @NoArgsConstructor
    public static class DeliveryRateRequest {
        private String ddrRgn;
        private BigDecimal ddrAm;
        private BigDecimal ddrEAm;
        private String ddrDes;
    }

    @Getter @NoArgsConstructor
    public static class SubmitCheckRequest {
        private Boolean qscRqYn;
        private String qscDes;
        private String qscKey;
        private Boolean qscDfltYn;
        private Integer qscSort;
    }

    @Getter @NoArgsConstructor
    public static class FeePolicyRequest {
        private String bgpGr;
        private String fpFeeTy;
        private String fpCalcTy;
        private BigDecimal fpVal;
        // Front-end <input type="date"> sends "yyyy-MM-dd" — accept as LocalDate
        private LocalDate fpEfFrDt;
        private LocalDate fpEfToDt;
        private String fpDes;
    }

    @Getter @NoArgsConstructor
    public static class UnitDiscountReq {
        private Long unGId;
        private Integer unDMinQn;
        private BigDecimal unDMinAm;
        private BigDecimal unDQnDr;
        private BigDecimal unDAmDr;
        private String unDOvTy;    // HIGHER / LOWER / MULTIPLY / FIXED
        private String unDDes;
    }

}
