package com.goodee.beedan.quoteTests;

import com.goodee.beedan.dto.quote.QuoteShipFeeRequest;
import com.goodee.beedan.entity.QuoteShipFee;
import com.goodee.beedan.repository.quote.QuoteShipFeeRepository;
import com.goodee.beedan.service.quote.QuoteShipFeeService;
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
class QuoteShipFeeTests {

    @Autowired
    private QuoteShipFeeRepository quoteShipFeeRepository;

    @Autowired
    private QuoteShipFeeService quoteShipFeeService;

    @Test
    @DisplayName("QuoteShipFee 전체 흐름 확인")
    void quoteShipFeeCrudTest() {

        // ── 1. CREATE ─────────────────────────────────
        log.info("========== 1. CREATE ==========");

        QuoteShipFeeRequest createRequest = QuoteShipFeeRequest.builder()
                .quInfoId(1L)
                .quId(1L)
                .ngId(1L)
                .faId(1L)
                .qsfFaNm("도쿄 1공장")
                .qsfFaCCd("JP")
                .qsfTrspTy("SEA")
                .qsfTtDz(7)
                .build();

        QuoteShipFee created = quoteShipFeeService.create(createRequest);

        log.info("생성된 배송비 ID : {}", created.getQsfId());
        log.info("공장명           : {}", created.getQsfFaNm());
        log.info("국가코드         : {}", created.getQsfFaCCd());
        log.info("운송 수단        : {}", created.getQsfTrspTy());
        log.info("총 다스 수량     : {}", created.getQsfUnQn());
        log.info("수동 운임 여부   : {}", created.getQsfSrYn());
        log.info("보험 가입 여부   : {}", created.getQsfInsYn());

        // ── 2. READ ───────────────────────────────────
        log.info("========== 2. READ ==========");

        QuoteShipFee foundById = quoteShipFeeService.findById(created.getQsfId());
        log.info("단건 조회        : ID={}, 공장={}", foundById.getQsfId(), foundById.getQsfFaNm());

        List<QuoteShipFee> allByQuoteInfo = quoteShipFeeService.findAllByQuoteInfo(1L);
        log.info("견적상세1 배송비 : {}개", allByQuoteInfo.size());

        // ── 3. 해외 운임 저장 ─────────────────────────
        log.info("========== 3. 해외 운임 저장 ==========");

        quoteShipFeeService.setShippingFee(created.getQsfId(), new BigDecimal("80000"));
        log.info("해외 운임        : {}원", created.getQsfSrAm());
        log.info("수동 운임 여부   : {}", created.getQsfSrYn());

        // ── 4. 항만/통관 비용 저장 ────────────────────
        log.info("========== 4. 항만/통관 비용 저장 ==========");

        quoteShipFeeService.setPortCustomsFee(
                created.getQsfId(),
                new BigDecimal("50000"),
                new BigDecimal("60000"),
                new BigDecimal("10000"));

        log.info("항만 비용        : {}원", created.getQsfPrtAm());
        log.info("통관 수수료      : {}원", created.getQsfCstAm());
        log.info("HS Code 신고료   : {}원", created.getQsfHsCd());

        // ── 5. 보험료 저장 ────────────────────────────
        log.info("========== 5. 보험료 저장 ==========");

        quoteShipFeeService.setInsurance(created.getQsfId(), new BigDecimal("5000"));
        log.info("보험 가입 여부   : {}", created.getQsfInsYn());
        log.info("보험료           : {}원", created.getQsfInsAm());

        // ── 6. CIF 계산 ───────────────────────────────
        log.info("========== 6. CIF 계산 ==========");

        BigDecimal itemTotal = new BigDecimal("1000000");
        quoteShipFeeService.calculateCif(created.getQsfId(), itemTotal);
        log.info("상품가           : {}원", itemTotal);
        log.info("해외운임         : {}원", created.getQsfSrAm());
        log.info("보험료           : {}원", created.getQsfInsAm());
        log.info("CIF              : {}원", created.getQsfCifAm());

        // ── 7. 관세/부가세 계산 ───────────────────────
        log.info("========== 7. 관세/부가세 계산 ==========");

        quoteShipFeeService.calculateDutyAndVat(
                created.getQsfId(), new BigDecimal("0.0800"));

        log.info("관세율           : {}%", created.getQsfDtyR().multiply(new BigDecimal("100")));
        log.info("관세액           : {}원", created.getQsfDty());
        log.info("부가세액         : {}원", created.getQsfVat());

        // ── 8. 최종 합계 계산 ─────────────────────────
        log.info("========== 8. 최종 합계 계산 ==========");

        quoteShipFeeService.calculateTotal(created.getQsfId());

        log.info("해외 운임        : {}원", created.getQsfSrAm());
        log.info("항만 비용        : {}원", created.getQsfPrtAm());
        log.info("통관 수수료      : {}원", created.getQsfCstAm());
        log.info("HS Code 신고료   : {}원", created.getQsfHsCd());
        log.info("보험료           : {}원", created.getQsfInsAm());
        log.info("관세액           : {}원", created.getQsfDty());
        log.info("부가세액         : {}원", created.getQsfVat());
        log.info("할인율           : {}", created.getQsfDscR());
        log.info("할인금액         : {}원", created.getQsfDscAm());
        log.info("최종 합계        : {}원", created.getQsfTtl());

        // ── 9. 수동 운임 override ─────────────────────
        log.info("========== 9. 수동 운임 override ==========");

        quoteShipFeeService.overrideShippingFee(
                created.getQsfId(),
                new BigDecimal("150000"),
                "오리털 패딩 특수화물 추가요금");

        log.info("수정된 운임      : {}원", created.getQsfSrAm());
        log.info("수동 운임 여부   : {}", created.getQsfSrYn());
        log.info("수정 사유        : {}", created.getQsfSrDes());

        // ── 10. 전체 배송비 합산 ─────────────────────
        log.info("========== 10. 전체 배송비 합산 ==========");

        BigDecimal totalShipFee = quoteShipFeeService.calculateTotalShipFee(1L);
        log.info("견적상세1 전체 배송비 합계: {}원", totalShipFee);

        log.info("========== 전체 흐름 완료 ==========");
    }
}