package com.goodee.beedan.repository.quote;

import com.goodee.beedan.entity.HsCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HsCodeRepository extends JpaRepository<HsCode, Long> {

    Optional<HsCode> findByCatId(Long catId);
}
