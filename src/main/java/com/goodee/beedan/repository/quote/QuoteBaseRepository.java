package com.goodee.beedan.repository.quote;

import com.goodee.beedan.common.constant.QuoteStatus;
import com.goodee.beedan.entity.QuoteBase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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

    // 수신자별 견적 페이징 조회
    Page<QuoteBase> findAllByQuRid(Long quRid, Pageable pageable);

    // 송신자 또는 수신자별 견적 페이징 조회
    @Query(
            "SELECT q FROM QuoteBase q WHERE q.quSid = :memId OR q.quRid = :memId ORDER BY q.quCreDt DESC")
    Page<QuoteBase> findAllByMember(@Param("memId") Long memId, Pageable pageable);

    // 기간별 상태 조회
    List<QuoteBase> findAllByQuSttAndQuCreDtBetween(QuoteStatus quStt, LocalDateTime from, LocalDateTime to);

    // 기간별 전체 조회 (최신순)
    List<QuoteBase> findAllByQuCreDtBetweenOrderByQuCreDtDesc(LocalDateTime from, LocalDateTime to);

    // 기간별 상태별 카운트
    long countByQuSttAndQuCreDtBetween(QuoteStatus quStt, LocalDateTime from, LocalDateTime to);
}