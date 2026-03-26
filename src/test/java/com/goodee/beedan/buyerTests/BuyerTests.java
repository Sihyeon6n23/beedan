package com.goodee.beedan.buyerTests;

import com.goodee.beedan.dto.buyer.BuyerRequest;
import com.goodee.beedan.entity.Buyer;
import com.goodee.beedan.repository.buyer.BuyerRepository;
import com.goodee.beedan.service.buyer.BuyerService;
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
class BuyerTests {

    @Autowired
    private BuyerRepository buyerRepository;

    @Autowired
    private BuyerService buyerService;

    @Test
    @DisplayName("Buyer CRUD 전체 흐름 확인")
    void buyerCrudTest() {

        // ── 1. CREATE ─────────────────────────────────
        log.info("========== 1. CREATE ==========");

        BuyerRequest request = new BuyerRequest("999-99-99999", "주식회사 테스트");
        Buyer created = buyerService.createOrFind(request);

        log.info("생성된 고객사 ID    : {}", created.getById());
        log.info("사업자번호          : {}", created.getMemBizNo());
        log.info("상호명              : {}", created.getMemBizTtl());
        log.info("등급                : {}", created.getBgpGr());
        log.info("누적 거래 횟수      : {}", created.getByOrdCnt());
        log.info("누적 거래 금액      : {}", created.getByTtlAm());
        log.info("최초 거래일         : {}", created.getByFrDt());
        log.info("최근 거래일         : {}", created.getByLtDt());

        // ── 2. READ ───────────────────────────────────
        log.info("========== 2. READ ==========");

        Buyer foundById = buyerService.findById(created.getById());
        log.info("ID로 조회           : {}", foundById.getMemBizTtl());

        Buyer foundByBizNo = buyerService.findByBizNo("999-99-99999");
        log.info("사업자번호로 조회   : {}", foundByBizNo.getMemBizTtl());

        List<Buyer> standardBuyers = buyerService.findAllByGrade(created.getBgpGr());
        log.info("등급별 조회 결과    : {}개", standardBuyers.size());
        standardBuyers.forEach(b ->
                log.info("  └── 사업자번호: {}, 상호명: {}, 등급: {}",
                        b.getMemBizNo(), b.getMemBizTtl(), b.getBgpGr()));

        // ── 3. UPDATE (실적 업데이트) ─────────────────
        log.info("========== 3. UPDATE (실적 업데이트) ==========");
        log.info("업데이트 전 누적 거래 횟수: {}", created.getByOrdCnt());
        log.info("업데이트 전 누적 거래 금액: {}", created.getByTtlAm());

        buyerService.updateAfterPayment(created.getById(), new BigDecimal("1000000"));

        log.info("업데이트 후 누적 거래 횟수: {}", created.getByOrdCnt());
        log.info("업데이트 후 누적 거래 금액: {}", created.getByTtlAm());
        log.info("최초 거래일               : {}", created.getByFrDt());
        log.info("최근 거래일               : {}", created.getByLtDt());

        // ── 4. UPDATE (등급 일괄 재산정) ─────────────
        log.info("========== 4. UPDATE (등급 일괄 재산정) ==========");
        log.info("재산정 전 등급: {}", created.getBgpGr());

        buyerService.bulkResolveGrade();

        Buyer afterGrade = buyerService.findById(created.getById());
        log.info("재산정 후 등급: {}", afterGrade.getBgpGr());

        // ── 5. DELETE (중복 사업자번호 → 기존 반환) ──
        log.info("========== 5. 중복 사업자번호 createOrFind ==========");

        Buyer duplicate = buyerService.createOrFind(request);
        log.info("중복 요청 결과 ID   : {}", duplicate.getById());
        log.info("기존과 동일한가     : {}", created.getById().equals(duplicate.getById()));

        log.info("========== CRUD 전체 흐름 완료 ==========");
    }
}