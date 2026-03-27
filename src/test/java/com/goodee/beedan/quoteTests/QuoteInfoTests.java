package com.goodee.beedan.quoteTests;

import com.goodee.beedan.dto.quote.QuoteInfoRequest;
import com.goodee.beedan.entity.QuoteInfo;
import com.goodee.beedan.repository.quote.QuoteInfoRepository;
import com.goodee.beedan.service.quote.QuoteInfoService;
import groovy.util.logging.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@lombok.extern.slf4j.Slf4j
@Slf4j
@SpringBootTest
@Transactional
class QuoteInfoTests {

    @Autowired
    private QuoteInfoRepository quoteInfoRepository;

    @Autowired
    private QuoteInfoService quoteInfoService;

    @Test
    @DisplayName("QuoteInfo CRUD 전체 흐름 확인")
    void quoteInfoCrudTest() {

        // ── 1. CREATE ─────────────────────────────────
        log.info("========== 1. CREATE ==========");

        QuoteInfoRequest createRequest = QuoteInfoRequest.builder()
                .quId(3L)
                .ngId(2L)
                .quInfoCurCd("JPY")
                .quInfoExcRt(new BigDecimal("9.345678"))
                .bgpId(1L)
                .fpId(1L)
                .quInfoDsrDt(LocalDateTime.now().plusDays(14))
                .quInfoPs("긴급 주문")
                .build();

        QuoteInfo created = quoteInfoService.create(createRequest);

        log.info("생성된 견적상세 ID : {}", created.getQuInfoId());
        log.info("견적 아이디        : {}", created.getQuId());
        log.info("협상 아이디        : {}", created.getNgId());
        log.info("통화 코드          : {}", created.getQuInfoCurCd());
        log.info("적용 환율          : {}", created.getQuInfoExcRt());
        log.info("적용 등급 ID       : {}", created.getBgpId());
        log.info("적용 정책 ID       : {}", created.getFpId());
        log.info("희망 수령일        : {}", created.getQuInfoDsrDt());
        log.info("특기사항           : {}", created.getQuInfoPs());

        // ── 2. READ ───────────────────────────────────
        log.info("========== 2. READ ==========");

        QuoteInfo foundById = quoteInfoService.findById(created.getQuInfoId());
        log.info("단건 조회          : ID={}, 통화={}", foundById.getQuInfoId(), foundById.getQuInfoCurCd());

        QuoteInfo foundByQuoteId = quoteInfoService.findByQuoteId(3L);
        log.info("견적ID로 조회      : ID={}", foundByQuoteId.getQuInfoId());

        List<QuoteInfo> allByNego = quoteInfoService.findAllByNego(2L);
        log.info("협상2 견적상세 수  : {}개", allByNego.size());

        // ── 3. 서비스 수수료 계산 ─────────────────────
        log.info("========== 3. 서비스 수수료 계산 ==========");

        BigDecimal itemTotal = new BigDecimal("1000000");
        BigDecimal feeRate = new BigDecimal("0.0500");
        BigDecimal discountRate = new BigDecimal("0.1000");

        QuoteInfo afterFee = quoteInfoService.calculateServiceFee(
                created.getQuInfoId(), itemTotal, feeRate, discountRate);

        log.info("상품 금액          : {}원", itemTotal);
        log.info("수수료율           : {}%", feeRate.multiply(new BigDecimal("100")));
        log.info("서비스 수수료      : {}원", afterFee.getQuInfoSrvFe());
        log.info("할인율             : {}%", discountRate.multiply(new BigDecimal("100")));
        log.info("할인 후 수수료     : {}원", afterFee.getQuInfoSrvFeAm());

        // ── 4. 국내 배송비 저장 ───────────────────────
        log.info("========== 4. 국내 배송비 저장 ==========");

        QuoteInfo afterDelivery = quoteInfoService.setDomesticDelivery(
                created.getQuInfoId(),
                new BigDecimal("3000"),
                new BigDecimal("3000"));

        log.info("국내 배송비        : {}원", afterDelivery.getQuInfoDdAm());
        log.info("도서산간 추가      : {}원", afterDelivery.getQuInfoDdExAm());

        // ── 5. 최종 합계 계산 ─────────────────────────
        log.info("========== 5. 최종 합계 계산 ==========");

        BigDecimal intShipTotal = new BigDecimal("80000");
        BigDecimal taxTotal = new BigDecimal("50000");

        QuoteInfo afterTotal = quoteInfoService.calculateTotal(
                created.getQuInfoId(), itemTotal, intShipTotal, taxTotal);

        log.info("상품 금액          : {}원", itemTotal);
        log.info("국제 배송비 합계   : {}원", afterTotal.getQuInfoIntShiFe());
        log.info("전체 배송비 합계   : {}원", afterTotal.getQuInfoTtlShiFe());
        log.info("관부가세           : {}원", afterTotal.getQuInfoTax());
        log.info("최종 합계          : {}원", afterTotal.getQuInfoTp());

        log.info("========== CRUD 전체 흐름 완료 ==========");
    }
}