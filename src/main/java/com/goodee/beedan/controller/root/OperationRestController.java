package com.goodee.beedan.controller.root;

import com.goodee.beedan.entity.*;
import com.goodee.beedan.repository.buyer.BuyerGradePolicyRepository;
import com.goodee.beedan.repository.quote.*;
import com.goodee.beedan.service.quote.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/root/operation")
@RequiredArgsConstructor
public class OperationRestController {

    private final BuyerGradePolicyRepository buyerGradePolicyRepository;
    private final ShippingInsuranceRepository shippingInsuranceRepository;
    private final StockInspectionRepository stockInspectionRepository;
    private final com.goodee.beedan.repository.quote.UnitGroupRepository unitGroupRepository;
    private final com.goodee.beedan.repository.quote.ShippingRateRepository shippingRateRepository;
    private final com.goodee.beedan.repository.quote.PortCustomsRateRepository portCustomsRateRepository;
    private final com.goodee.beedan.repository.quote.DomesticDeliveryRateRepository domesticDeliveryRateRepository;
    private final QuoteSubmitCheckRepository quoteSubmitCheckRepository;
    private final com.goodee.beedan.repository.buyer.FeePolicyRepository feePolicyRepository;

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
                .bgpEfFrDt(req.getBgpEfFrDt())
                .bgpEfToDt(req.getBgpEfToDt())
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
                req.getBgpEfFrDt(), req.getBgpEfToDt(), req.getBgpDes());
        buyerGradePolicyRepository.save(grade);
        return ResponseEntity.ok(Map.of("status", "ok"));
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
        UnitGroup ug = UnitGroup.builder().unGNm(req.getUnGNm()).unGQn(req.getUnGQn()).unGYn(true).build();
        unitGroupRepository.save(ug);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PutMapping("/unit-group/{id}")
    public ResponseEntity<?> updateUnitGroup(@PathVariable Long id, @RequestBody UnitGroupRequest req) {
        UnitGroup ug = unitGroupRepository.findById(id).orElseThrow();
        ug.update(req.getUnGNm(), req.getUnGQn());
        unitGroupRepository.save(ug);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @DeleteMapping("/unit-group/{id}")
    public ResponseEntity<?> deleteUnitGroup(@PathVariable Long id) {
        UnitGroup ug = unitGroupRepository.findById(id).orElseThrow();
        ug.deactivate();
        unitGroupRepository.save(ug);
        return ResponseEntity.ok(Map.of("status", "ok"));
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
        QuoteSubmitCheck qsc = QuoteSubmitCheck.builder()
                .qscRqYn(req.getQscRqYn())
                .qscDes(req.getQscDes())
                .qscKey(req.getQscKey())
                .qscDfltYn(req.getQscDfltYn())
                .qscSort(req.getQscSort())
                .build();
        quoteSubmitCheckRepository.save(qsc);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PutMapping("/submit-check/{id}")
    public ResponseEntity<?> updateSubmitCheck(@PathVariable Long id, @RequestBody SubmitCheckRequest req) {
        QuoteSubmitCheck qsc = quoteSubmitCheckRepository.findById(id).orElseThrow();
        qsc.update(req.getQscRqYn(), req.getQscDes(), req.getQscKey(), req.getQscDfltYn(), req.getQscSort());
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
                .fpEfFrDt(req.getFpEfFrDt())
                .fpEfToDt(req.getFpEfToDt())
                .fpDes(req.getFpDes())
                .build();
        feePolicyRepository.save(fp);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PutMapping("/fee-policy/{id}")
    public ResponseEntity<?> updateFeePolicy(@PathVariable Long id, @RequestBody FeePolicyRequest req) {
        FeePolicy fp = feePolicyRepository.findById(id).orElseThrow();
        fp.update(req.getFpCalcTy(), req.getFpVal(), req.getFpEfFrDt(), req.getFpEfToDt(), req.getFpDes());
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
        private LocalDateTime bgpEfFrDt;
        private LocalDateTime bgpEfToDt;
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
        private LocalDateTime fpEfFrDt;
        private LocalDateTime fpEfToDt;
        private String fpDes;
    }
}
