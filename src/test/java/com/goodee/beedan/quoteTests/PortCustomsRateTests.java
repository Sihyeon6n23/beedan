package com.goodee.beedan.quoteTests;

import com.goodee.beedan.dto.quote.PortCustomsRateRequest;
import com.goodee.beedan.dto.quote.PortCustomsRateUpdateRequest;
import com.goodee.beedan.entity.PortCustomsRate;
import com.goodee.beedan.repository.quote.PortCustomsRateRepository;
import com.goodee.beedan.service.quote.PortCustomsRateService;
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
class PortCustomsRateTests {

    @Autowired
    private PortCustomsRateRepository portCustomsRateRepository;

    @Autowired
    private PortCustomsRateService portCustomsRateService;

    @Test
    @DisplayName("PortCustomsRate CRUD 전체 흐름 확인")
    void portCustomsRateCrudTest() {

        // ── 1. CREATE ─────────────────────────────────
        log.info("========== 1. CREATE ==========");

        PortCustomsRateRequest createRequest = PortCustomsRateRequest.builder()
                .pcrTy("INSPECTION")
                .pcrSmAm(new BigDecimal("20000"))
                .pcrMdAm(new BigDecimal("35000"))
                .pcrLgAm(new BigDecimal("50000"))
                .pcrDes("검사비")
                .build();

        PortCustomsRate created = portCustomsRateService.create(createRequest);

        log.info("생성된 정책 ID  : {}", created.getPcrId());
        log.info("비용 종류       : {}", created.getPcrTy());
        log.info("소형 금액       : {}원", created.getPcrSmAm());
        log.info("중형 금액       : {}원", created.getPcrMdAm());
        log.info("대형 금액       : {}원", created.getPcrLgAm());
        log.info("활성 여부       : {}", created.getPcrYn());
        log.info("관리자 메모     : {}", created.getPcrDes());

        // ── 2. READ ───────────────────────────────────
        log.info("========== 2. READ ==========");

        PortCustomsRate foundById = portCustomsRateService.findById(created.getPcrId());
        log.info("단건 조회       : {} / 소형{}원", foundById.getPcrTy(), foundById.getPcrSmAm());

        PortCustomsRate portRate = portCustomsRateService.findActiveByType("PORT");
        log.info("PORT 조회       : 소형{}원 / 중형{}원 / 대형{}원",
                portRate.getPcrSmAm(), portRate.getPcrMdAm(), portRate.getPcrLgAm());

        List<PortCustomsRate> allActive = portCustomsRateService.findAllActive();
        log.info("전체 활성 정책  : {}개", allActive.size());
        allActive.forEach(p ->
                log.info("  └── 종류: {}, 소형: {}원, 중형: {}원, 대형: {}원",
                        p.getPcrTy(), p.getPcrSmAm(), p.getPcrMdAm(), p.getPcrLgAm()));

        // ── 3. getApplicableAmount 확인 ───────────────
        log.info("========== 3. getApplicableAmount ==========");
        log.info("PORT SMALL  → {}원", portRate.getApplicableAmount("SMALL"));
        log.info("PORT MEDIUM → {}원", portRate.getApplicableAmount("MEDIUM"));
        log.info("PORT LARGE  → {}원", portRate.getApplicableAmount("LARGE"));

        // ── 4. UPDATE ─────────────────────────────────
        log.info("========== 4. UPDATE ==========");
        log.info("수정 전 소형 금액: {}원", created.getPcrSmAm());

        PortCustomsRateUpdateRequest updateRequest = PortCustomsRateUpdateRequest.builder()
                .pcrSmAm(new BigDecimal("25000"))
                .pcrMdAm(new BigDecimal("40000"))
                .pcrLgAm(new BigDecimal("55000"))
                .pcrDes("검사비 인상")
                .build();

        PortCustomsRate updated = portCustomsRateService.update(created.getPcrId(), updateRequest);

        log.info("수정 후 소형 금액: {}원", updated.getPcrSmAm());
        log.info("수정 후 중형 금액: {}원", updated.getPcrMdAm());
        log.info("수정 후 대형 금액: {}원", updated.getPcrLgAm());
        log.info("수정 후 메모     : {}", updated.getPcrDes());

        // ── 5. 중복 등록 예외 확인 ────────────────────
        log.info("========== 5. 중복 등록 예외 확인 ==========");

        try {
            portCustomsRateService.create(createRequest);
        } catch (IllegalStateException e) {
            log.info("중복 등록 예외 발생: {}", e.getMessage());
        }

        // ── 6. DELETE (비활성화) ──────────────────────
        log.info("========== 6. DELETE (비활성화) ==========");
        log.info("비활성화 전 활성 여부: {}", updated.getPcrYn());

        portCustomsRateService.deactivate(updated.getPcrId());

        log.info("비활성화 후 활성 여부: {}", updated.getPcrYn());

        List<PortCustomsRate> afterDeactivate = portCustomsRateService.findAllActive();
        log.info("비활성화 후 활성 정책 수: {}개", afterDeactivate.size());

        log.info("========== CRUD 전체 흐름 완료 ==========");
    }
}