package com.goodee.beedan.quoteTests;

import com.goodee.beedan.common.constant.OverlapType;
import com.goodee.beedan.dto.quote.UnitDiscountRequest;
import com.goodee.beedan.entity.UnitDiscount;
import com.goodee.beedan.repository.quote.UnitDiscountRepository;
import com.goodee.beedan.service.quote.UnitDiscountService;
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
class UnitDiscountTests {

    @Autowired
    private UnitDiscountRepository unitDiscountRepository;

    @Autowired
    private UnitDiscountService unitDiscountService;

    @Test
    @DisplayName("UnitDiscount CRUD 전체 흐름 확인")
    void unitDiscountCrudTest() {

        // ── 1. CREATE ─────────────────────────────────
        log.info("========== 1. CREATE ==========");

        UnitDiscountRequest createRequest = UnitDiscountRequest.builder()
                .unGId(2L)
                .unDMinQn(3)
                .unDMinAm(null)
                .unDQnDr(new BigDecimal("0.0200"))
                .unDAmDr(BigDecimal.ZERO)
                .unDOvTy(OverlapType.HIGHER)
                .unDDes("3다스 이상 2% 할인")
                .build();

        UnitDiscount created = unitDiscountService.create(createRequest);

        log.info("생성된 정책 ID    : {}", created.getUnDId());
        log.info("묶음 단위 아이디  : {}", created.getUnGId());
        log.info("최소 묶음 수량    : {}", created.getUnDMinQn());
        log.info("수량 할인율       : {}", created.getUnDQnDr());
        log.info("중복 처리 방식    : {}", created.getUnDOvTy());
        log.info("관리자 메모       : {}", created.getUnDDes());

        // ── 2. READ ───────────────────────────────────
        log.info("========== 2. READ ==========");

        UnitDiscount foundById = unitDiscountService.findById(created.getUnDId());
        log.info("단건 조회         : ID={}, 최소수량={}", foundById.getUnDId(), foundById.getUnDMinQn());

        List<UnitDiscount> allByUnit = unitDiscountService.findAllByUnitGroup(2L);
        log.info("묶음 단위2 할인 정책 수: {}개", allByUnit.size());
        allByUnit.forEach(d ->
                log.info("  └── ID: {}, 최소수량: {}, 수량할인율: {}, 금액할인율: {}, 방식: {}",
                        d.getUnDId(), d.getUnDMinQn(), d.getUnDQnDr(), d.getUnDAmDr(), d.getUnDOvTy()));

        // ── 3. calculateDiscount 확인 ─────────────────
        log.info("========== 3. calculateDiscount ==========");

        BigDecimal rate1 = unitDiscountService.calculateDiscount(2L, 7, new BigDecimal("300000"));
        log.info("7다스 / 30만원 → 최종 할인율: {}", rate1);

        BigDecimal rate2 = unitDiscountService.calculateDiscount(2L, 3, new BigDecimal("600000"));
        log.info("3다스 / 60만원 → 최종 할인율: {}", rate2);

        BigDecimal rate3 = unitDiscountService.calculateDiscount(2L, 12, new BigDecimal("700000"));
        log.info("12다스 / 70만원 → 최종 할인율: {}", rate3);

        // ── 4. UPDATE ─────────────────────────────────
        log.info("========== 4. UPDATE ==========");
        log.info("수정 전 수량 할인율: {}", created.getUnDQnDr());

        UnitDiscountRequest updateRequest = UnitDiscountRequest.builder()
                .unGId(2L)
                .unDMinQn(3)
                .unDMinAm(null)
                .unDQnDr(new BigDecimal("0.0400"))
                .unDAmDr(BigDecimal.ZERO)
                .unDOvTy(OverlapType.HIGHER)
                .unDDes("3다스 이상 4% 할인으로 상향")
                .build();

        UnitDiscount updated = unitDiscountService.update(created.getUnDId(), updateRequest);

        log.info("수정 후 새 정책 ID    : {}", updated.getUnDId());
        log.info("수정 후 수량 할인율   : {}", updated.getUnDQnDr());
        log.info("수정 후 관리자 메모   : {}", updated.getUnDDes());

        // ── 5. DELETE ─────────────────────────────────
        log.info("========== 5. DELETE ==========");

        unitDiscountService.delete(updated.getUnDId());
        log.info("삭제 완료. ID: {}", updated.getUnDId());

        List<UnitDiscount> afterDelete = unitDiscountService.findAllByUnitGroup(2L);
        log.info("삭제 후 묶음 단위2 할인 정책 수: {}개", afterDelete.size());

        log.info("========== CRUD 전체 흐름 완료 ==========");
    }
}