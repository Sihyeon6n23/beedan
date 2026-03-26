package com.goodee.beedan.repository.buyer;

import com.goodee.beedan.entity.BuyerGradePolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BuyerGradePolicyRepository extends JpaRepository<BuyerGradePolicy, Long> {

    // 등급별 활성 정책 조회
    Optional<BuyerGradePolicy> findByBgpGrAndBgpAcYnTrue(String bgpGr);
    // 활성 정책 전체 조회
    List<BuyerGradePolicy> findAllByBgpAcYnTrue();
    // 활성 정책 전체 조회 (
    List<BuyerGradePolicy> findAllByBgpAcYnTrueOrderByBgpMinOrdCntDesc();

    // 가장 낮은 등급 조회 (거래 횟수 기준)
    Optional<BuyerGradePolicy> findTopByBgpAcYnTrueOrderByBgpMinOrdCntAsc();




}
