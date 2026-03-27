package com.goodee.beedan.repository.quote;

import com.goodee.beedan.common.constant.QuoteStatus;
import com.goodee.beedan.entity.QuoteBase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuoteBaseRepository extends JpaRepository<QuoteBase, Long> {

    // 협상별 견적 전체 조회
    List<QuoteBase> findAllByNgId(Long ngId);

    // 협상별 특정 상태 견적 조회
    List<QuoteBase> findAllByNgIdAndQuStt(Long ngId, QuoteStatus quStt);

    // 송신자별 견적 조회
    List<QuoteBase> findAllByQuSid(Long quSid);

    // 수신자별 견적 조회
    List<QuoteBase> findAllByQuRid(Long quRid);
}