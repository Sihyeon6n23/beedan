package com.goodee.beedan.repository.quote;

import com.goodee.beedan.entity.QuoteSubmitCheckLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuoteSubmitCheckLogRepository extends JpaRepository<QuoteSubmitCheckLog, Long> {

    // 견적별 체크 로그 조회
    List<QuoteSubmitCheckLog> findAllByQuId(Long quId);

    // 견적 + 특정 체크 항목 조회 (알림 발송 판별용)
    QuoteSubmitCheckLog findByQuIdAndQscId(Long quId, Long qscId);
}
