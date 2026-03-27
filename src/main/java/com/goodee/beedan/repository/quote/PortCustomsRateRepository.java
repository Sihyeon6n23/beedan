package com.goodee.beedan.repository.quote;

import com.goodee.beedan.entity.PortCustomsRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortCustomsRateRepository extends JpaRepository<PortCustomsRate, Long> {

    // 비용 종류로 활성 정책 조회 (중복 체크 + 견적 계산 시 사용)
    Optional<PortCustomsRate> findByPcrTyAndPcrYnTrue(String pcrTy);

    // 활성 정책 전체 조회
    List<PortCustomsRate> findAllByPcrYnTrue();
}