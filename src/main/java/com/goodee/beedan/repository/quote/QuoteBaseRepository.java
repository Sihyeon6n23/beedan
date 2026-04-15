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

    // 협상별 유효 견적 조회 (quStt != null + 상대방 TEMP_SAVE 제외)
    @Query("SELECT q FROM QuoteBase q WHERE q.ngId = :ngId AND q.quStt IS NOT NULL AND (q.quStt <> 'TEMP_SAVE' OR q.quSid = :memId OR (q.quSid IS NULL AND q.quRid = :memId)) ORDER BY q.quCreDt DESC")
    List<QuoteBase> findAllActiveByNgId(@Param("ngId") Long ngId, @Param("memId") Long memId);

    // 협상별 유효 견적 조회 (admin용 - TEMP_SAVE 포함)
    @Query("SELECT q FROM QuoteBase q WHERE q.ngId = :ngId AND q.quStt IS NOT NULL ORDER BY q.quCreDt DESC")
    List<QuoteBase> findAllActiveByNgIdAdmin(@Param("ngId") Long ngId);

    // 전체 유효 견적 조회 (quStt != null + 상대방 TEMP_SAVE 제외)
    @Query("SELECT q FROM QuoteBase q WHERE q.quStt IS NOT NULL AND (q.quStt <> 'TEMP_SAVE' OR q.quSid = :memId OR (q.quSid IS NULL AND q.quRid = :memId)) ORDER BY q.quCreDt DESC")
    List<QuoteBase> findAllActive(@Param("memId") Long memId);

    // 전체 유효 견적 조회 (파라미터 없는 버전 - 하위호환)
    @Query("SELECT q FROM QuoteBase q WHERE q.quStt IS NOT NULL ORDER BY q.quCreDt DESC")
    List<QuoteBase> findAllActiveNoFilter();

    // 협상별 특정 상태 견적 조회
    List<QuoteBase> findAllByNgIdAndQuStt(Long ngId, QuoteStatus quStt);

    // 송신자별 견적 조회
    List<QuoteBase> findAllByQuSid(Long quSid);

    // 수신자별 견적 조회
    List<QuoteBase> findAllByQuRid(Long quRid);

    // 수신자별 견적 페이징 조회
    Page<QuoteBase> findAllByQuRid(Long quRid, Pageable pageable);

    // 송신자 또는 수신자별 유효 견적 페이징 조회 (상대방의 TEMP_SAVE 제외)
    @Query(
            "SELECT q FROM QuoteBase q WHERE q.quStt IS NOT NULL AND (q.quSid = :memId OR q.quRid = :memId) AND (q.quStt <> 'TEMP_SAVE' OR q.quSid = :memId OR (q.quSid IS NULL AND q.quRid = :memId)) ORDER BY q.quCreDt DESC")
    Page<QuoteBase> findAllByMember(@Param("memId") Long memId, Pageable pageable);

    // 견적 + 협상명 조인 조회 — 사용자용 (N+1 제거)
    @Query("SELECT q.quId, q.quStt, q.quUsOpYn, q.quAdOpYn, q.quCd, q.quCreDt, q.quUpdDt, q.quSid, q.quRid, n.ngNm " +
           "FROM QuoteBase q JOIN Negotiation n ON q.ngId = n.ngId " +
           "WHERE q.quStt IS NOT NULL AND (q.quSid = :memId OR q.quRid = :memId) " +
           "AND (q.quStt <> 'TEMP_SAVE' OR q.quSid = :memId OR (q.quSid IS NULL AND q.quRid = :memId)) " +
           "ORDER BY q.quCreDt DESC")
    Page<Object[]> findAllByMemberWithNgNm(@Param("memId") Long memId, Pageable pageable);

    // 견적 + 협상명 조인 조회 — 어드민용 전체 (N+1 제거)
    @Query("SELECT q.quId, q.quStt, q.quAdOpYn, q.quCd, q.quCreDt, q.quUpdDt, q.quSid, q.quRid, n.ngNm " +
           "FROM QuoteBase q JOIN Negotiation n ON q.ngId = n.ngId " +
           "WHERE q.quStt IS NOT NULL AND q.quStt <> 'TEMP_SAVE' ORDER BY q.quCreDt DESC")
    List<Object[]> findAllActiveWithNgNmForAdmin();

    // 견적 + 협상명 조인 조회 — 어드민용 협상별 (N+1 제거)
    @Query("SELECT q.quId, q.quStt, q.quAdOpYn, q.quCd, q.quCreDt, q.quUpdDt, q.quSid, q.quRid, n.ngNm " +
           "FROM QuoteBase q JOIN Negotiation n ON q.ngId = n.ngId " +
           "WHERE q.ngId = :ngId AND q.quStt IS NOT NULL ORDER BY q.quCreDt DESC")
    List<Object[]> findAllActiveWithNgNmByNgIdForAdmin(@Param("ngId") Long ngId);

    // 기간별 상태 조회
    List<QuoteBase> findAllByQuSttAndQuCreDtBetween(QuoteStatus quStt, LocalDateTime from, LocalDateTime to);

    // 기간별 전체 조회 (최신순)
    List<QuoteBase> findAllByQuCreDtBetweenOrderByQuCreDtDesc(LocalDateTime from, LocalDateTime to);

    // 기간별 상태별 카운트
    long countByQuSttAndQuCreDtBetween(QuoteStatus quStt, LocalDateTime from, LocalDateTime to);

    // 상태별 건수 한 번에 (GROUP BY)
    @Query("SELECT q.quStt, COUNT(q) FROM QuoteBase q WHERE q.quStt IS NOT NULL AND q.quCreDt BETWEEN :from AND :to GROUP BY q.quStt")
    List<Object[]> countGroupByStatus(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // 요일별 견적 건수 (TEMP_SAVE 제외)
    @Query("SELECT DAYOFWEEK(q.quCreDt), COUNT(q) FROM QuoteBase q WHERE q.quStt IS NOT NULL AND q.quStt <> 'TEMP_SAVE' AND q.quCreDt BETWEEN :from AND :to GROUP BY DAYOFWEEK(q.quCreDt)")
    List<Object[]> countGroupByDayOfWeek(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}