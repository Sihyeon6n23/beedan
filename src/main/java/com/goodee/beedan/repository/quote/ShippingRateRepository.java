package com.goodee.beedan.repository.quote;

import com.goodee.beedan.common.constant.TransportType;
import com.goodee.beedan.entity.ShippingRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShippingRateRepository extends JpaRepository<ShippingRate, Long> {

    // 국가코드 + 운송수단으로 활성 운임표 조회 (견적 계산 시 핵심)
    Optional<ShippingRate> findBySrCCdAndSrTrspTyAndSrYnTrue(
            String srCCd, TransportType srTrspTy);

    // 국가코드별 활성 운임표 전체 조회
    List<ShippingRate> findAllBySrCCdAndSrYnTrue(String srCCd);

    // 활성 운임표 전체 조회
    List<ShippingRate> findAllBySrYnTrue();

    // 활성 운임이 등록된 국가 코드 목록
    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT sr.srCCd FROM ShippingRate sr WHERE sr.srYn = true ORDER BY sr.srCCd")
    List<String> findDistinctCountryCodes();
}