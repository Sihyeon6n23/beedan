package com.goodee.beedan.repository.board;

import com.goodee.beedan.common.constant.BoardType;
import com.goodee.beedan.common.constant.InquiryStatus;
import com.goodee.beedan.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    // 1. 공통: 삭제 안 된 게시글 찾기 (관리자 문의 상세)
    Optional<Board> findByBrdIdAndBrdTyAndBrdDelYnFalse(Long brdId, BoardType brdTy);

    // 2. 공통: 삭제 안 된 게시글 페이징 목록
    Page<Board> findByBrdTyAndBrdDelYnFalse(BoardType brdTy, Pageable pageable);

    // 3. 공지 특화: 상단 고정글 목록
    List<Board> findByBrdTyAndBrdFixYnTrueAndBrdDelYnFalse(BoardType brdTy);

    // 4. 공지 특화: 조회수 증가 쿼리
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Board b SET b.brdVstCnt = b.brdVstCnt + 1 WHERE b.brdId = :brdId AND b.brdTy = :brdTy AND b.brdDelYn = false")
    int increaseViewCount(@Param("brdId") Long brdId, @Param("brdTy") BoardType brdTy);
    // ===============================================================


    // ===============================================================

    // 사용자 문의 목록 (본인 + 문의 + 삭제X + 제목 검색 + 상태 필터 + 페이지네이션)
    @Query("""
        SELECT b FROM Board b
        WHERE b.brdTy = :brdTy
          AND b.brdDelYn = false
          AND b.member.memId = :memId
          AND (:brdInqStt IS NULL OR b.brdInqStt = :brdInqStt)
          AND (:keyword IS NULL OR b.brdTtl LIKE %:keyword%)
        ORDER BY b.brdCreDt DESC
    """)
    Page<Board> findUserInquiryBoards(
        @Param("brdTy") BoardType brdTy,
        @Param("memId") Long memId,
        @Param("brdInqStt") InquiryStatus brdInqStt,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    // 관리자 전체 문의 목록 (문의 + 삭제X + 제목/회사 상호명 검색 + 상태 필터 + 내 답변 여부)
    @Query("""
        SELECT b FROM Board b
        JOIN b.member m
        WHERE b.brdTy = :brdTy
          AND b.brdDelYn = false
          AND (:brdInqStt IS NULL OR b.brdInqStt = :brdInqStt)
          AND (:keyword IS NULL OR b.brdTtl LIKE %:keyword% OR m.memBizTtl LIKE %:keyword%)
        ORDER BY
          CASE
            WHEN b.brdInqStt = 'RECEIVED' THEN 0
            WHEN b.brdInqStt = 'IN_PROGRESS' THEN 1
            WHEN b.brdInqStt = 'ANSWERED' THEN 2
            ELSE 3
          END,
          b.brdUpdDt DESC
    """)
    Page<Board> findAdminInquiryBoards(
        @Param("brdTy") BoardType brdTy,
        @Param("brdInqStt") InquiryStatus brdInqStt,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    // 관리자 본인 답글 문의 목록 (내가 답글을 작성한 문의 + 제목/회사 상호명 검색 + 상태 필터)
    @Query("""
        SELECT b FROM Board b
        JOIN b.member m
        WHERE b.brdTy = :brdTy
          AND b.brdDelYn = false
          AND EXISTS (
              SELECT 1
              FROM Board r
              WHERE r.brdPrnId = b.brdId
                AND r.brdTy = :ansbrdTy
                AND r.member.memId = :memAdId
                AND r.brdDelYn = false
          )
          AND (:brdInqStt IS NULL OR b.brdInqStt = :brdInqStt)
          AND (:keyword IS NULL OR b.brdTtl LIKE %:keyword% OR m.memBizTtl LIKE %:keyword%)
        ORDER BY
          CASE
            WHEN b.brdInqStt = 'RECEIVED' THEN 0
            WHEN b.brdInqStt = 'IN_PROGRESS' THEN 1
            WHEN b.brdInqStt = 'ANSWERED' THEN 2
          ELSE 3
        END,
        b.brdUpdDt DESC
    """)
    Page<Board> findInquiryBoardsAnsweredByAdmin(
        @Param("brdTy") BoardType brdTy,
        @Param("ansbrdTy") BoardType ansbrdTy,
        @Param("memAdId") Long memAdId,
        @Param("brdInqStt") InquiryStatus brdInqStt,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    // 사용자 상세
    Optional<Board> findByBrdIdAndBrdTyAndMember_MemIdAndBrdDelYnFalse(Long brdId, BoardType brdTy, Long memId);

    // 관리자 상세 (공통 메서드 부분에)
//    Optional<Board> findByBrdIdAndBrdTyAndBrdDelYnFalse(Long brdId, BoardType brdTy);

    // 답글 단일 조회
    Optional<Board> findByBrdPrnIdAndBrdTyAndBrdDelYnFalse(Long brdPrnId, BoardType brdTy);

    // 답글 존재 여부
    boolean existsByBrdPrnIdAndBrdTyAndBrdDelYnFalse(Long brdPrnId, BoardType brdTy);
}
