package com.goodee.beedan.buyerTests;

import com.goodee.beedan.dto.buyer.FeePolicyCreateRequest;
import com.goodee.beedan.dto.buyer.FeePolicyUpdateRequest;
import com.goodee.beedan.entity.FeePolicy;
import com.goodee.beedan.repository.buyer.FeePolicyRepository;
import com.goodee.beedan.service.buyer.FeePolicyService;
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
class FeePolicyTests {

    @Autowired
    private FeePolicyRepository feePolicyRepository;

    @Autowired
    private FeePolicyService feePolicyService;

    @Test
    @DisplayName("FeePolicy CRUD 전체 흐름 확인")
    void feePolicyCrudTest() {

        // ── 1. CREATE ─────────────────────────────────
        log.info("========== 1. CREATE ==========");

        FeePolicyCreateRequest createRequest = new FeePolicyCreateRequest(
                "VIP", "URGENT_PROCESSING", "FIXED",
                new BigDecimal("30000"),
                LocalDateTime.now(), null, "VIP 긴급처리비 3만원"
        );

        FeePolicy created = feePolicyService.create(createRequest);

        log.info("생성된 정책 ID    : {}", created.getFpId());
        log.info("등급              : {}", created.getBgpGr());
        log.info("비용 항목         : {}", created.getFpFeeTy());
        log.info("계산 방식         : {}", created.getFpCalcTy());
        log.info("계산 값           : {}", created.getFpVal());
        log.info("활성 여부         : {}", created.getFpAcYn());
        log.info("적용 시작일       : {}", created.getFpEfFrDt());
        log.info("적용 종료일       : {}", created.getFpEfToDt());
        log.info("관리자 메모       : {}", created.getFpDes());

        // ── 2. READ ───────────────────────────────────
        log.info("========== 2. READ ==========");

        FeePolicy foundById = feePolicyService.findById(created.getFpId());
        log.info("단건 조회         : {} / {}", foundById.getBgpGr(), foundById.getFpFeeTy());

        List<FeePolicy> vipPolicies = feePolicyService.findAllActiveByGrade("VIP");
        log.info("VIP 활성 정책 수  : {}개", vipPolicies.size());
        vipPolicies.forEach(p ->
                log.info("  └── 항목: {}, 방식: {}, 값: {}",
                        p.getFpFeeTy(), p.getFpCalcTy(), p.getFpVal()));

        List<FeePolicy> allPolicies = feePolicyService.findAllActive();
        log.info("전체 활성 정책 수 : {}개", allPolicies.size());

        // ── 3. UPDATE ─────────────────────────────────
        log.info("========== 3. UPDATE ==========");
        log.info("업데이트 전 계산값  : {}", created.getFpVal());
        log.info("업데이트 전 활성여부: {}", created.getFpAcYn());

        FeePolicyUpdateRequest updateRequest = new FeePolicyUpdateRequest(
                "FIXED", new BigDecimal("50000"),
                LocalDateTime.now(), null, "VIP 긴급처리비 5만원으로 상향"
        );

        FeePolicy updated = feePolicyService.update(created.getFpId(), updateRequest);

        log.info("업데이트 후 기존 정책 활성여부  : {}", created.getFpAcYn());
        log.info("새로 생성된 정책 ID             : {}", updated.getFpId());
        log.info("새로 생성된 정책 계산값         : {}", updated.getFpVal());
        log.info("새로 생성된 정책 메모           : {}", updated.getFpDes());
        log.info("새로 생성된 정책 활성여부       : {}", updated.getFpAcYn());

        // ── 4. 중복 등록 예외 확인 ────────────────────
        log.info("========== 4. 중복 등록 예외 확인 ==========");

        FeePolicyCreateRequest duplicateRequest = new FeePolicyCreateRequest(
                "VIP", "URGENT_PROCESSING", "FIXED",
                new BigDecimal("30000"),
                LocalDateTime.now(), null, "중복 테스트"
        );

        try {
            feePolicyService.create(duplicateRequest);
        } catch (IllegalStateException e) {
            log.info("중복 등록 예외 발생 확인: {}", e.getMessage());
        }

        // ── 5. DELETE (비활성화) ──────────────────────
        log.info("========== 5. DELETE (비활성화) ==========");
        log.info("비활성화 전 활성여부: {}", updated.getFpAcYn());

        feePolicyService.deactivate(updated.getFpId());

        log.info("비활성화 후 활성여부: {}", updated.getFpAcYn());

        List<FeePolicy> afterDeactivate = feePolicyService.findAllActiveByGrade("VIP");
        log.info("비활성화 후 VIP 활성 정책 수: {}개", afterDeactivate.size());

        log.info("========== CRUD 전체 흐름 완료 ==========");
    }
}