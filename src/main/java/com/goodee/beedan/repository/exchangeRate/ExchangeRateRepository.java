package com.goodee.beedan.repository.exchangeRate;

import com.goodee.beedan.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {


    // 통화별 최신 환율 조회 (기준 시간 내림차순 첫 번째)
    Optional<ExchangeRate> findTopByErCrOrderByErFDtDesc(String erCr);

    // 통화별 환율 이력 전체 조회 (최신순)
    List<ExchangeRate> findAllByErCrOrderByErFDtDesc(String erCr);

}
