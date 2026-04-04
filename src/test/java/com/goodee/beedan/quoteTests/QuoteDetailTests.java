package com.goodee.beedan.quoteTests;

import com.goodee.beedan.dto.quote.QuoteDetailRequest;
import com.goodee.beedan.entity.QuoteDetail;
import com.goodee.beedan.repository.quote.QuoteDetailRepository;
import com.goodee.beedan.service.quote.QuoteDetailService;
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
class QuoteDetailTests {

    @Autowired
    private QuoteDetailRepository quoteDetailRepository;

    @Autowired
    private QuoteDetailService quoteDetailService;

    @Test
    @DisplayName("QuoteDetail CRUD 전체 흐름 확인")
    void quoteDetailCrudTest() {

        // ── 1. CREATE (단건) ──────────────────────────
        log.info("========== 1. CREATE (단건) ==========");

        QuoteDetailRequest createRequest = QuoteDetailRequest.builder()
                .quInfoId(1L)
                .quId(1L)
                .ngId(1L)
                .stId(4L)
                .stNm("니트 스웨터")
                .quDtQn(60)
                .faId(1L)
                .faNm("도쿄 1공장")
                .unGId(2L)
                .unGNm("다스")
                .quUQn(5)
                .quDtFgPr(new BigDecimal("3200.0000"))
                //.quDtRe("긴급 주문")
                .build();

        QuoteDetail created = quoteDetailService.create(createRequest);

        log.info("생성된 품목 ID  : {}", created.getQuDtId());
        log.info("상품명          : {}", created.getStNm());
        log.info("상품 수량       : {}", created.getQuDtQn());
        log.info("공장명          : {}", created.getFaNm());
        log.info("묶음 단위       : {}", created.getUnGNm());
        log.info("묶음 수량       : {}", created.getQuUQn());
        log.info("외화 단가       : {}", created.getQuDtFgPr());
        log.info("원화 단가       : {}", created.getQuDtKrPr());
        log.info("원화 합계       : {}", created.getQuDtPr());

        // ── 2. CREATE (다건) ──────────────────────────
        log.info("========== 2. CREATE (다건) ==========");

        List<QuoteDetailRequest> multiRequests = List.of(
                QuoteDetailRequest.builder()
                        .quInfoId(1L).quId(1L).ngId(1L)
                        .stId(5L).stNm("반팔 셔츠").quDtQn(120)
                        .faId(2L).faNm("오사카 1공장")
                        .unGId(2L).unGNm("다스").quUQn(10)
                        .quDtFgPr(new BigDecimal("1500.0000"))
                        .build(),
                QuoteDetailRequest.builder()
                        .quInfoId(1L).quId(1L).ngId(1L)
                        .stId(6L).stNm("긴팔 셔츠").quDtQn(60)
                        .faId(2L).faNm("오사카 1공장")
                        .unGId(2L).unGNm("다스").quUQn(5)
                        .quDtFgPr(new BigDecimal("1800.0000"))
                        .build()
        );

        List<QuoteDetail> createdAll = quoteDetailService.createAll(multiRequests);
        log.info("다건 등록 완료  : {}개", createdAll.size());
        createdAll.forEach(d ->
                log.info("  └── 상품명: {}, 수량: {}, 공장: {}, 묶음수: {}",
                        d.getStNm(), d.getQuDtQn(), d.getFaNm(), d.getQuUQn()));

        // ── 3. READ ───────────────────────────────────
        log.info("========== 3. READ ==========");

        QuoteDetail foundById = quoteDetailService.findById(created.getQuDtId());
        log.info("단건 조회       : {} / {}개", foundById.getStNm(), foundById.getQuDtQn());

        List<QuoteDetail> allByQuoteInfo = quoteDetailService.findAllByQuoteInfo(1L);
        log.info("견적상세1 품목  : {}개", allByQuoteInfo.size());
        allByQuoteInfo.forEach(d ->
                log.info("  └── 상품명: {}, 공장: {}, 묶음수: {}",
                        d.getStNm(), d.getFaNm(), d.getQuUQn()));

        // ── 4. 공장별 조회 + 총 다스 수량 합산 ───────
        log.info("========== 4. 공장별 조회 + 총 다스 수량 ==========");

        List<QuoteDetail> factory1Details =
                quoteDetailService.findAllByQuoteInfoAndFactory(1L, 1L);
        log.info("도쿄 1공장 품목 : {}개", factory1Details.size());

        Integer totalDozen1 = quoteDetailService.calculateTotalDozen(1L, 1L);
        log.info("도쿄 1공장 총 다스: {}다스", totalDozen1);

        Integer totalDozen2 = quoteDetailService.calculateTotalDozen(1L, 2L);
        log.info("오사카 1공장 총 다스: {}다스", totalDozen2);

        // ── 5. 원화 환산 계산 ─────────────────────────
        log.info("========== 5. 원화 환산 계산 ==========");
        log.info("환산 전 원화 단가: {}", created.getQuDtKrPr());
        log.info("환산 전 원화 합계: {}", created.getQuDtPr());

        BigDecimal exchangeRate = new BigDecimal("9.345678");
        QuoteDetail calculated = quoteDetailService
                .calculateKrwPrice(created.getQuDtId(), exchangeRate);

        log.info("환율             : {}", exchangeRate);
        log.info("환산 후 원화 단가: {}원", calculated.getQuDtKrPr());
        log.info("환산 후 원화 합계: {}원", calculated.getQuDtPr());

        // ── 6. DELETE ─────────────────────────────────
        log.info("========== 6. DELETE ==========");

        quoteDetailService.delete(created.getQuDtId());
        log.info("삭제 완료. ID: {}", created.getQuDtId());

        List<QuoteDetail> afterDelete = quoteDetailService.findAllByQuoteInfo(1L);
        log.info("삭제 후 견적상세1 품목: {}개", afterDelete.size());

        log.info("========== CRUD 전체 흐름 완료 ==========");
    }
}