package com.goodee.beedan.repository.quote;

import com.goodee.beedan.entity.UnitDiscount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UnitDiscountRepository extends JpaRepository<UnitDiscount, Long> {

    // 묶음 단위별 할인 정책 전체 조회
    List<UnitDiscount> findAllByUnGId(Long unGId);
}