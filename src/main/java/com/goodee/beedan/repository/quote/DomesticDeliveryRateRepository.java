package com.goodee.beedan.repository.quote;

import com.goodee.beedan.entity.DomesticDeliveryRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DomesticDeliveryRateRepository extends JpaRepository<DomesticDeliveryRate, Long> {

    // 지역 구분으로 활성 배송비 조회 (중복 체크 + 견적 계산 시 사용)
    Optional<DomesticDeliveryRate> findByDdrRgnAndDdrYnTrue(String ddrRgn);

    // 활성 배송비 전체 조회
    List<DomesticDeliveryRate> findAllByDdrYnTrue();
}