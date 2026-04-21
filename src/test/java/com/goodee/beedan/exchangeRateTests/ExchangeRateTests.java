package com.goodee.beedan.exchangeRateTests;

import com.goodee.beedan.dto.exchangeRate.ExchangeRateRequest;
import com.goodee.beedan.entity.ExchangeRate;
import com.goodee.beedan.repository.exchangeRate.ExchangeRateRepository;
import com.goodee.beedan.service.exchangeRate.ExchangeRateService;
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
class ExchangeRateTests {

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Test
    @DisplayName("ExchangeRate CRUD 전체 흐름 확인")
    void exchangeRateCrudTest() {

        // ── 1. CREATE ─────────────────────────────────
        log.info("========== 1. CREATE ==========");

        ExchangeRateRequest createRequest = ExchangeRateRequest.builder()
                .erCr("JPY")
                .erRa(new BigDecimal("9.345678"))
                .erFDt(LocalDateTime.now())
                .build();

        ExchangeRate created = exchangeRateService.create(createRequest);

        log.info("생성된 환율 ID  : {}", created.getErId());
        log.info("통화            : {}", created.getErCr());
        log.info("환율            : {}", created.getErRa());
        log.info("기본 통화       : {}", created.getErBa());
        log.info("환율 기준 시간  : {}", created.getErFDt());
        log.info("생성 시간       : {}", created.getErCrDt());

        // ── 2. READ ───────────────────────────────────
        log.info("========== 2. READ ==========");

        ExchangeRate foundById = exchangeRateService.findById(created.getErId());
        log.info("단건 조회       : {} / {}", foundById.getErCr(), foundById.getErRa());

        ExchangeRate latestJpy = exchangeRateService.findLatestByCurrency("JPY");
        log.info("JPY 최신 환율   : {} (기준시간: {})",
                latestJpy.getErRa(), latestJpy.getErFDt());

        List<ExchangeRate> jpyHistory = exchangeRateService.findAllByCurrency("JPY");
        log.info("JPY 환율 이력   : {}개", jpyHistory.size());
        jpyHistory.forEach(e ->
                log.info("  └── 환율: {}, 기준시간: {}", e.getErRa(), e.getErFDt()));

        // ── 3. toKrw / toForeign 확인 ─────────────────
        log.info("========== 3. toKrw / toForeign ==========");

        BigDecimal foreignAmount = new BigDecimal("10000");
        BigDecimal krwAmount = latestJpy.toKrw(foreignAmount);
        log.info("10,000엔 → 원화: {}원", krwAmount);

        BigDecimal backToForeign = latestJpy.toForeign(krwAmount);
        log.info("{}원 → 엔화: {}엔", krwAmount, backToForeign);

        log.info("========== CRUD 전체 흐름 완료 ==========");
    }
}