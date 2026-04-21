package com.goodee.beedan.quoteTests;

import com.goodee.beedan.common.constant.TransportType;
import com.goodee.beedan.dto.quote.ShippingRateRequest;
import com.goodee.beedan.dto.quote.ShippingRateUpdateRequest;
import com.goodee.beedan.entity.ShippingRate;
import com.goodee.beedan.repository.quote.ShippingRateRepository;
import com.goodee.beedan.service.quote.ShippingRateService;
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
class ShippingRateTests {

    @Autowired
    private ShippingRateRepository shippingRateRepository;

    @Autowired
    private ShippingRateService shippingRateService;

    @Test
    @DisplayName("ShippingRate CRUD 전체 흐름 확인")
    void shippingRateCrudTest() {

        // ── 1. CREATE ─────────────────────────────────
        log.info("========== 1. CREATE ==========");

        ShippingRateRequest createRequest = ShippingRateRequest.builder()
                .srCCd("US")
                .srTrspTy(TransportType.SEA)
                .srSmQn(1).srSmAm(new BigDecimal("150000"))
                .srMdQn(5).srMdAm(new BigDecimal("250000"))
                .srLgQn(10).srLgAm(new BigDecimal("350000"))
                .srDes("미국 해상 운임")
                .build();

        ShippingRate created = shippingRateService.create(createRequest);

        log.info("생성된 운임표 ID : {}", created.getSrId());
        log.info("국가코드         : {}", created.getSrCCd());
        log.info("운송 수단        : {}", created.getSrTrspTy());
        log.info("소형 기준/운임   : {}다스 / {}원", created.getSrSmQn(), created.getSrSmAm());
        log.info("중형 기준/운임   : {}다스 / {}원", created.getSrMdQn(), created.getSrMdAm());
        log.info("대형 기준/운임   : {}다스 / {}원", created.getSrLgQn(), created.getSrLgAm());
        log.info("활성 여부        : {}", created.getSrYn());

        // ── 2. READ ───────────────────────────────────
        log.info("========== 2. READ ==========");

        ShippingRate foundById = shippingRateService.findById(created.getSrId());
        log.info("단건 조회        : {} / {}", foundById.getSrCCd(), foundById.getSrTrspTy());

        ShippingRate jpSea = shippingRateService
                .findByCCdAndTransportType("JP", TransportType.SEA);
        log.info("JP SEA 운임표    : 소형 {}원 / 중형 {}원 / 대형 {}원",
                jpSea.getSrSmAm(), jpSea.getSrMdAm(), jpSea.getSrLgAm());

        List<ShippingRate> jpRates = shippingRateService.findAllByCountry("JP");
        log.info("일본 운임표 수   : {}개", jpRates.size());

        List<ShippingRate> allActive = shippingRateService.findAllActive();
        log.info("전체 활성 운임표 : {}개", allActive.size());

        // ── 3. getApplicableAmount / getSizeType 확인 ─
        log.info("========== 3. getApplicableAmount / getSizeType ==========");
        log.info("JP SEA 3다스  → 사이즈: {}, 운임: {}원",
                jpSea.getSizeType(3), jpSea.getApplicableAmount(3));
        log.info("JP SEA 7다스  → 사이즈: {}, 운임: {}원",
                jpSea.getSizeType(7), jpSea.getApplicableAmount(7));
        log.info("JP SEA 12다스 → 사이즈: {}, 운임: {}원",
                jpSea.getSizeType(12), jpSea.getApplicableAmount(12));

        // ── 4. UPDATE ─────────────────────────────────
        log.info("========== 4. UPDATE ==========");
        log.info("수정 전 소형 운임: {}원", created.getSrSmAm());

        ShippingRateUpdateRequest updateRequest = ShippingRateUpdateRequest.builder()
                .srSmAm(new BigDecimal("160000"))
                .srMdAm(new BigDecimal("260000"))
                .srLgAm(new BigDecimal("360000"))
                .srDes("미국 해상 운임 인상")
                .build();

        ShippingRate updated = shippingRateService.update(created.getSrId(), updateRequest);

        log.info("수정 후 소형 운임: {}원", updated.getSrSmAm());
        log.info("수정 후 중형 운임: {}원", updated.getSrMdAm());
        log.info("수정 후 대형 운임: {}원", updated.getSrLgAm());
        log.info("수정 후 메모     : {}", updated.getSrDes());

        // ── 5. 중복 등록 예외 확인 ────────────────────
        log.info("========== 5. 중복 등록 예외 확인 ==========");

        try {
            shippingRateService.create(createRequest);
        } catch (IllegalStateException e) {
            log.info("중복 등록 예외 발생: {}", e.getMessage());
        }

        // ── 6. DELETE (비활성화) ──────────────────────
        log.info("========== 6. DELETE (비활성화) ==========");
        log.info("비활성화 전 활성 여부: {}", updated.getSrYn());

        shippingRateService.deactivate(updated.getSrId());

        log.info("비활성화 후 활성 여부: {}", updated.getSrYn());

        List<ShippingRate> afterDeactivate = shippingRateService.findAllActive();
        log.info("비활성화 후 활성 운임표 수: {}개", afterDeactivate.size());

        log.info("========== CRUD 전체 흐름 완료 ==========");
    }
}