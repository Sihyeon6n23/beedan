package com.goodee.beedan.repository.board;

import com.goodee.beedan.common.constant.BoardType;
import com.goodee.beedan.common.constant.InquiryStatus;
import com.goodee.beedan.entity.Board;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    // 1. 공통: 삭제 안 된 특정 게시글 찾기
    Optional<Board> findByBrdIdAndBrdTyAndBrdDelYnFalse(Long brdId, BoardType brdTy);

    // 2. 공통: 삭제 안 된 게시글 페이징 목록
    Page<Board> findByBrdTyAndBrdDelYnFalse(BoardType brdTy, Pageable pageable);

    // 3. 공지 특화: 상단 고정글 목록
    List<Board> findByBrdTyAndBrdFixYnTrueAndBrdDelYnFalse(BoardType brdTy);

    // 4. 문의 특화: 내 작성글 페이징 목록
    Page<Board> findByBrdTyAndMemIdAndBrdDelYnFalse(BoardType brdTy, Long memId, Pageable pageable);

    // 5. 공통: 조회수 증가 쿼리
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Board b SET b.brdVstCnt = b.brdVstCnt + 1 WHERE b.brdId = :brdId AND b.brdTy = :brdTy AND b.brdDelYn = false")
    int increaseViewCount(@Param("brdId") Long brdId, @Param("brdTy") BoardType brdTy);
    // ===============================================================


    // ===============================================================

    // 사용자 문의 목록 (본인 + 문의 + 삭제X + 제목검색 + 상태 필터 + 페이지네이션)
    @Query("""
        SELECT b FROM Board b
        WHERE b.brdTy = :boardType
          AND b.brdDelYn = false
          AND b.memId = :memberId
          AND (:status IS NULL OR b.brdInqStt = :status)
          AND (:keyword IS NULL OR b.brdTtl LIKE %:keyword%)
        ORDER BY b.brdCreDt DESC
    """)
    Page<Board> findUserInquiryBoards(
        @Param("boardType") BoardType boardType,
        @Param("memberId") Long memberId,
        @Param("status") InquiryStatus status,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    // 관리자 전체 문의 목록 (문의 + 삭제X + 제목/회사 상호명 검색 + 상태 필터 + 내 답변 여부)
    @Query("""
        SELECT b FROM Board b
        JOIN Member m ON b.memId = m.memId
        WHERE b.brdTy = :boardType
          AND b.brdDelYn = false
          AND (:status IS NULL OR b.brdInqStt = :status)
          AND (:keyword IS NULL OR b.brdTtl LIKE %:keyword%)
          AND (:bizTitle IS NULL OR m.memBizTtl LIKE %:bizTitle%)
        ORDER BY
          CASE
            WHEN b.brdInqStt = 'RECEIVED' THEN 0
            WHEN b.brdInqStt = 'IN_PROGRESS' THEN 1
            WHEN b.brdInqStt = 'ANSWERED' THEN 2
            ELSE 3
          END,
          b.brdCreDt DESC
    """)
    Page<Board> findAdminInquiryBoards(
        @Param("boardType") BoardType boardType,
        @Param("status") InquiryStatus status,
        @Param("keyword") String keyword,
        @Param("bizTitle") String bizTitle,
        Pageable pageable
    );

    // 관리자 본인 문의 목록 (내가 답글을 작성한 문의 + 제목/회사 상호명 검색 + 상태 필터)
    @Query("""
        SELECT b FROM Board b
        JOIN Member m ON b.memId = m.memId
        WHERE b.brdTy = :boardType
          AND b.brdDelYn = false
          AND EXISTS (
              SELECT 1
              FROM Board a
              WHERE a.brdPrnId = b.brdId
                AND a.brdTy = :answerType
                AND a.memId = :adminId
                AND a.brdDelYn = false
          )
          AND (:status IS NULL OR b.brdInqStt = :status)
          AND (:keyword IS NULL OR b.brdTtl LIKE %:keyword%)
          AND (:bizTitle IS NULL OR m.memBizTtl LIKE %:bizTitle%)
        ORDER BY
          CASE
            WHEN b.brdInqStt = 'RECEIVED' THEN 0
            WHEN b.brdInqStt = 'IN_PROGRESS' THEN 1
            WHEN b.brdInqStt = 'ANSWERED' THEN 2
          ELSE 3
        END,
        b.brdCreDt DESC
    """)
    Page<Board> findInquiryBoardsAnsweredByAdmin(
        @Param("boardType") BoardType boardType,
        @Param("answerType") BoardType answerType,
        @Param("adminId") Long adminId,
        @Param("status") InquiryStatus status,
        @Param("keyword") String keyword,
        @Param("bizTitle") String bizTitle,
        Pageable pageable
    );

    // 사용자 상세
    Optional<Board> findByBrdIdAndBrdTyAndMemIdAndBrdDelYnFalse(Long boardId, BoardType boardType, Long memberId);

    // 관리자 상세
//    Optional<Board> findByBrdIdAndBrdTyAndBrdDelYnFalse(Long boardId, BoardType boardType);

    // 답글 단일 조회
    Optional<Board> findByBrdPrnIdAndBrdTyAndBrdDelYnFalse(Long parentId, BoardType boardType);

    // 답글 존재 여부
    boolean existsByBrdPrnIdAndBrdTyAndBrdDelYnFalse(Long parentId, BoardType boardType);
}
