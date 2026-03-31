package com.goodee.beedan.repository.quote;

import com.goodee.beedan.entity.QuoteInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuoteInfoRepository extends JpaRepository<QuoteInfo, Long> {

    // 견적 아이디로 조회
    Optional<QuoteInfo> findByQuId(Long quId);

    // 협상 아이디로 전체 조회
    List<QuoteInfo> findAllByNgId(Long ngId);

    List<QuoteInfo> findByNgId(Long ngId);
}