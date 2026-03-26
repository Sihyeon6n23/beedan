package com.goodee.beedan.buyerTests;

import com.goodee.beedan.dto.buyer.BuyerGradePolicyCreateRequest;
import com.goodee.beedan.dto.buyer.BuyerGradePolicyUpdateRequest;
import com.goodee.beedan.entity.BuyerGradePolicy;
import com.goodee.beedan.repository.buyer.BuyerGradePolicyRepository;
import com.goodee.beedan.service.buyer.BuyerGradePolicyService;
import groovy.util.logging.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@lombok.extern.slf4j.Slf4j
@SpringBootTest
@Transactional
@Slf4j
public class BuyerGradePolicyTests {

    @Autowired
    private BuyerGradePolicyRepository buyerGradePolicyRepository;

    @Autowired
    private BuyerGradePolicyService buyerGradePolicyService;

    @Test
    @DisplayName("활성 정책 전체 조회")
    void buyerGradePolicyCRUDTest() {
        log.info("");
        log.info("");
        log.info("");
        log.info("========== 1. CREATE ==========");

        BuyerGradePolicyCreateRequest createRequest1 =
                BuyerGradePolicyCreateRequest.builder()
                        .bgpGr("GOLD")
                        .bgpMinOrdCnt(10)
                        .bgpMinTtAm(new BigDecimal("30000000"))
                        .bgpEfFrDt(LocalDateTime.now())
                        .build();

        BuyerGradePolicyCreateRequest createRequest2 =
                BuyerGradePolicyCreateRequest.builder()
                        .bgpGr("SILVER")
                        .bgpMinOrdCnt(3)
                        .bgpMinTtAm(new BigDecimal("5000000"))
                        .bgpEfFrDt(LocalDateTime.now())
                        .build();

        BuyerGradePolicyCreateRequest createRequest3 =
                BuyerGradePolicyCreateRequest.builder()
                        .bgpGr("BRONZE")
                        .bgpEfFrDt(LocalDateTime.now())
                        .build();

        BuyerGradePolicy created = buyerGradePolicyService.create(createRequest1);
        log.info(created.getBgpGr() + "등급 정책 저장 완료");
        BuyerGradePolicy created2 = buyerGradePolicyService.create(createRequest2);
        log.info(created2.getBgpGr() + "등급 정책 저장 완료");
        BuyerGradePolicy created3 = buyerGradePolicyService.create(createRequest3);
        log.info(created3.getBgpGr() + "등급 정책 저장 완료");

        log.info("");
        log.info("");
        log.info("");
        log.info("========== 2. READ ==========");
        BuyerGradePolicy found = buyerGradePolicyService.findById(created.getBgpId());

        log.info("단건 조회 ID      : {}", found.getBgpId());
        log.info("단건 조회 등급    : {}", found.getBgpGr());
        log.info("단건 조회 활성여부: {}", found.getBgpAcYn());

        List<BuyerGradePolicy> allActive = buyerGradePolicyService.findAllActiveOrdered();
        log.info("활성 정책 전체 수 : {}개", allActive.size());
        allActive.forEach(p ->
                log.info("  └── 등급: {}, 최소거래횟수: {}, 최소금액: {}, 활성: {}",
                        p.getBgpGr(), p.getBgpMinOrdCnt(), p.getBgpMinTtAm(), p.getBgpAcYn()));

        assertThat(found.getBgpId()).isEqualTo(created.getBgpId());
        assertThat(allActive).isNotEmpty();

        log.info("");
        log.info("");
        log.info("");
        log.info("========== 3. UPDATE ==========");
        log.info("업데이트 전 최소 거래 횟수: {}", created.getBgpMinOrdCnt());
        log.info("업데이트 전 최소 누적 금액: {}", created.getBgpMinTtAm());
        log.info("업데이트 전 활성 여부      : {}", created.getBgpAcYn());

        BuyerGradePolicyUpdateRequest updateRequest =
                BuyerGradePolicyUpdateRequest.builder()
                        .bgpMinOrdCnt(20)
                        .bgpMinTtAm(new BigDecimal("150000000"))
                        .bgpEfFrDt(LocalDateTime.now())
                        .bgpDes("GOLD 등급 상향")
                        .build();

        BuyerGradePolicy updated =
                buyerGradePolicyService.update(created.getBgpId(), updateRequest);

        log.info("업데이트 후 기존 정책 활성여부  : {}", updated.getBgpAcYn());
        log.info("업데이트 후 정책 최소 거래 횟수 : {}", updated.getBgpMinOrdCnt());
        log.info("업데이트 후 정책 최소 누적 금액 : {}", updated.getBgpMinTtAm());
        log.info("업데이트 후 정책 설명           : {}", updated.getBgpDes());
        log.info("업데이트 후 정책 활성 여부      : {}", updated.getBgpAcYn());

        log.info("");
        log.info("");
        log.info("");
        log.info("========== 4. DELETE (비활성화) ==========");
        log.info("비활성화 전 활성 여부: {}", updated.getBgpAcYn());

        buyerGradePolicyService.deactivate(updated.getBgpId());

        log.info("비활성화 후 활성 여부: {}", updated.getBgpAcYn());

        List<BuyerGradePolicy> afterDeactivate =
                buyerGradePolicyService.findAllActive();
        log.info("비활성화 후 활성 정책 수: {}개", afterDeactivate.size());


        log.info("========== BuyerGradePolicy CRUD 끝 ==========");
        log.info("");
        log.info("");
        log.info("");
    }
}