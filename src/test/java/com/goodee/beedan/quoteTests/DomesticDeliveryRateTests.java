package com.goodee.beedan.quoteTests;

import com.goodee.beedan.dto.quote.DomesticDeliveryRateRequest;
import com.goodee.beedan.entity.DomesticDeliveryRate;
import com.goodee.beedan.repository.quote.DomesticDeliveryRateRepository;
import com.goodee.beedan.service.quote.DomesticDeliveryRateService;
import groovy.util.logging.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@lombok.extern.slf4j.Slf4j
@Slf4j
@SpringBootTest
@Transactional
class DomesticDeliveryRateTests {

    @Autowired
    private DomesticDeliveryRateRepository domesticDeliveryRateRepository;

    @Autowired
    private DomesticDeliveryRateService domesticDeliveryRateService;

    @Test
    @DisplayName("DomesticDeliveryRate CRUD 전체 흐름 확인")
    void domesticDeliveryRateCrudTest() {

        // ── 1. CREATE ─────────────────────────────────
        log.info("========== 1. CREATE ==========");

        DomesticDeliveryRateRequest createRequest = DomesticDeliveryRateRequest.builder()
                .ddrRgn("SPECIAL")
                .ddrAm(new BigDecimal("5000"))
                .ddrEAm(new BigDecimal("2000"))
                .ddrDes("특별 지역 배송비")
                .build();

        DomesticDeliveryRate created = domesticDeliveryRateService.create(createRequest);

        log.info("생성된 배송비 ID : {}", created.getDdrId());
        log.info("지역 구분        : {}", created.getDdrRgn());
        log.info("기본 배송비      : {}원", created.getDdrAm());
        log.info("추가 배송비      : {}원", created.getDdrEAm());
        log.info("총 배송비        : {}원", created.totalAmount());
        log.info("활성 여부        : {}", created.getDdrYn());
        log.info("관리자 메모      : {}", created.getDdrDes());

        // ── 2. READ ───────────────────────────────────
        log.info("========== 2. READ ==========");

        DomesticDeliveryRate foundById = domesticDeliveryRateService.findById(created.getDdrId());
        log.info("단건 조회        : {} / {}원", foundById.getDdrRgn(), foundById.totalAmount());

        DomesticDeliveryRate jejuRate = domesticDeliveryRateService.findActiveByRegion("JEJU");
        log.info("JEJU 배송비      : 기본{}원 + 추가{}원 = 총{}원",
                jejuRate.getDdrAm(), jejuRate.getDdrEAm(), jejuRate.totalAmount());

        List<DomesticDeliveryRate> allActive = domesticDeliveryRateService.findAllActive();
        log.info("전체 활성 배송비 : {}개", allActive.size());
        allActive.forEach(r ->
                log.info("  └── 지역: {}, 기본: {}원, 추가: {}원, 총: {}원",
                        r.getDdrRgn(), r.getDdrAm(), r.getDdrEAm(), r.totalAmount()));

        // ── 3. UPDATE ─────────────────────────────────
        log.info("========== 3. UPDATE ==========");
        log.info("수정 전 기본 배송비: {}원", created.getDdrAm());
        log.info("수정 전 추가 배송비: {}원", created.getDdrEAm());

        DomesticDeliveryRateRequest updateRequest = DomesticDeliveryRateRequest.builder()
                .ddrRgn("SPECIAL")
                .ddrAm(new BigDecimal("6000"))
                .ddrEAm(new BigDecimal("3000"))
                .ddrDes("특별 지역 배송비 인상")
                .build();

        DomesticDeliveryRate updated = domesticDeliveryRateService
                .update(created.getDdrId(), updateRequest);

        log.info("수정 후 기본 배송비: {}원", updated.getDdrAm());
        log.info("수정 후 추가 배송비: {}원", updated.getDdrEAm());
        log.info("수정 후 총 배송비  : {}원", updated.totalAmount());
        log.info("수정 후 메모       : {}", updated.getDdrDes());

        // ── 4. 중복 등록 예외 확인 ────────────────────
        log.info("========== 4. 중복 등록 예외 확인 ==========");

        try {
            domesticDeliveryRateService.create(createRequest);
        } catch (IllegalStateException e) {
            log.info("중복 등록 예외 발생: {}", e.getMessage());
        }

        // ── 5. DELETE (비활성화) ──────────────────────
        log.info("========== 5. DELETE (비활성화) ==========");
        log.info("비활성화 전 활성 여부: {}", updated.getDdrYn());

        domesticDeliveryRateService.deactivate(updated.getDdrId());

        log.info("비활성화 후 활성 여부: {}", updated.getDdrYn());

        List<DomesticDeliveryRate> afterDeactivate = domesticDeliveryRateService.findAllActive();
        log.info("비활성화 후 활성 배송비 수: {}개", afterDeactivate.size());

        log.info("========== CRUD 전체 흐름 완료 ==========");
    }
}