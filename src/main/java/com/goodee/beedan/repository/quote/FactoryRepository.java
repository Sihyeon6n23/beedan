package com.goodee.beedan.repository.quote;

import com.goodee.beedan.entity.Factory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FactoryRepository extends JpaRepository<Factory, Long> {

    // 국가코드별 활성 공장 조회
    List<Factory> findAllByFaCCdAndFaYnTrue(String faCCd);

    // 활성 공장 전체 조회
    List<Factory> findAllByFaYnTrue();

    // 브랜드별 활성 공장 조회
    List<Factory> findAllByBrIdAndFaYnTrue(Long brId);
}