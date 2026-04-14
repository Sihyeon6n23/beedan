package com.goodee.beedan.repository.quote;

import com.goodee.beedan.entity.Negotiation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NegotiationRepository extends JpaRepository<Negotiation, Long> {

    // 회원별 협상 목록 조회
    @Query("SELECT n FROM Negotiation n WHERE n.memId = :memId ORDER BY n.ngCreDt DESC")
    List<Negotiation> findAllByMemId(@Param("memId") Long memId);

    // 회원별 협상 목록 페이징 조회
    @Query("SELECT n FROM Negotiation n WHERE n.memId = :memId ORDER BY n.ngCreDt DESC")
    Page<Negotiation> findAllByMemId(@Param("memId") Long memId, Pageable pageable);

    // 회원별 진행 중인 협상 조회 (종료일 없음)
    List<Negotiation> findAllByMemIdAndNgEndDtIsNull(Long memId);

    Negotiation findFirstByMemIdOrderByNgCreDtDesc(Long memId);
    Negotiation findByNgId(Long ngId);

    // 기간 내 협상 수
    long countByNgCreDtBetween(java.time.LocalDateTime from, java.time.LocalDateTime to);

    // 유효 견적이 존재하는 협상 + 견적 수 + 미열람 수 — 사용자용 (N+1 제거)
    @Query("SELECT n.ngId, n.ngNm, n.ngCreDt, n.ngEndDt, " +
           "COUNT(q), " +
           "SUM(CASE WHEN q.quUsOpYn IS NULL OR q.quUsOpYn = false THEN 1 ELSE 0 END) " +
           "FROM Negotiation n JOIN QuoteBase q ON q.ngId = n.ngId " +
           "WHERE n.memId = :memId AND q.quStt IS NOT NULL " +
           "AND (q.quStt <> 'TEMP_SAVE' OR q.quSid = :memId OR (q.quSid IS NULL AND q.quRid = :memId)) " +
           "GROUP BY n.ngId, n.ngNm, n.ngCreDt, n.ngEndDt " +
           "ORDER BY n.ngCreDt DESC")
    List<Object[]> findNegotiationsWithQuoteSummary(@Param("memId") Long memId);

    // 어드민용 — 전체 협상 + 견적 수 + 미열람 수 + 회원 정보 (N+1 제거, TEMP_SAVE 제외)
    @Query("SELECT n.ngId, n.ngNm, n.ngCreDt, n.ngEndDt, " +
           "COUNT(q), " +
           "SUM(CASE WHEN q.quAdOpYn IS NULL OR q.quAdOpYn = false THEN 1 ELSE 0 END), " +
           "MAX(q.quUpdDt), " +
           "m.memNm, m.memBizTtl " +
           "FROM Negotiation n JOIN QuoteBase q ON q.ngId = n.ngId " +
           "JOIN Member m ON n.memId = m.memId " +
           "WHERE q.quStt IS NOT NULL AND q.quStt <> 'TEMP_SAVE' " +
           "GROUP BY n.ngId, n.ngNm, n.ngCreDt, n.ngEndDt, m.memNm, m.memBizTtl " +
           "ORDER BY n.ngCreDt DESC")
    List<Object[]> findAllNegotiationsWithSummaryForAdmin();
}
