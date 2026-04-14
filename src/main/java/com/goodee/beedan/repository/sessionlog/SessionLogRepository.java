package com.goodee.beedan.repository.sessionlog;

import com.goodee.beedan.entity.SessionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SessionLogRepository extends JpaRepository<SessionLog, Long> {

    Optional<SessionLog> findBySlSsId(String slSsId);

    // 기간 내 평균 체류 시간 (초)
    @Query("SELECT AVG(s.slDuSec) FROM SessionLog s WHERE s.slDuSec IS NOT NULL AND s.slStDt BETWEEN :from AND :to")
    Double avgDurationBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // 기간 내 체류 시간 구간별 분포
    @Query("SELECT " +
            "SUM(CASE WHEN s.slDuSec < 60 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN s.slDuSec >= 60 AND s.slDuSec < 300 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN s.slDuSec >= 300 AND s.slDuSec < 900 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN s.slDuSec >= 900 AND s.slDuSec < 1800 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN s.slDuSec >= 1800 THEN 1 ELSE 0 END) " +
            "FROM SessionLog s WHERE s.slDuSec IS NOT NULL AND s.slStDt BETWEEN :from AND :to")
    Object[] durationDistribution(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
