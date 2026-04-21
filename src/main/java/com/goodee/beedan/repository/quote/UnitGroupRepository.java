package com.goodee.beedan.repository.quote;

import com.goodee.beedan.entity.UnitGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UnitGroupRepository extends JpaRepository<UnitGroup, Long> {

    // 단위명으로 조회 (중복 체크)
    Optional<UnitGroup> findByUnGNm(String unGNm);

    // 단위명 존재 여부
    boolean existsByUnGNm(String unGNm);

    // 활성 단위 전체 조회
    List<UnitGroup> findAllByUnGYnTrue();
}