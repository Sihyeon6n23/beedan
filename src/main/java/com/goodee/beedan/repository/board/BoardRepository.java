package com.goodee.beedan.repository.board;

import com.goodee.beedan.common.constant.BoardType;
import com.goodee.beedan.common.constant.InquiryStatus;
import com.goodee.beedan.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long>, JpaSpecificationExecutor<Board> {

    // 1. 공통: 삭제 안 된 게시글 찾기 (관리자 문의 상세)
    Optional<Board> findByBrdIdAndBrdTyAndBrdDelYnFalse(Long brdId, BoardType brdTy);

    // 2. 공통: 삭제 안 된 게시글 페이징 목록
    Page<Board> findByBrdTyAndBrdDelYnFalse(BoardType brdTy, Pageable pageable);

    // 4. 공지 특화: 조회수 증가 쿼리
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Board b SET b.brdVstCnt = b.brdVstCnt + 1 WHERE b.brdId = :brdId AND b.brdTy = :brdTy AND b.brdDelYn = false")
    int increaseViewCount(@Param("brdId") Long brdId, @Param("brdTy") BoardType brdTy);

    // ===============================================================

    // 이전글 (현재 ID보다 작으면서 가장 큰 ID)
    Optional<Board> findFirstByBrdIdLessThanAndBrdTyAndBrdDelYnFalseOrderByBrdIdDesc(Long brdId, BoardType brdTy);

    // 다음글 (현재 ID보다 크면서 가장 작은 ID)
    Optional<Board> findFirstByBrdIdGreaterThanAndBrdTyAndBrdDelYnFalseOrderByBrdIdAsc(Long brdId, BoardType brdTy);

    // 4. 고정 공지 (Fetch Join 추가로 쿼리 1회 감소)
    @Query("SELECT b FROM Board b JOIN FETCH b.member WHERE b.brdTy = :type AND b.brdFixYn = true AND b.brdDelYn = false")
    List<Board> findTopFixedNotices(@Param("type") BoardType type, Pageable pageable);

    @Query("select b from Board b join fetch b.member " +
            "where b.brdId = :id and b.brdTy = :ty and b.brdDelYn = false")
    Optional<Board> findDetailWithMember(@Param("id") Long id, @Param("ty") BoardType ty);

    @Query(value = "select b from Board b join fetch b.member " +
            "where b.brdTy = :ty and b.brdDelYn = false",
            countQuery = "select count(b) from Board b where b.brdTy = :ty and b.brdDelYn = false")
    Page<Board> findListWithMember(@Param("ty") BoardType brdTy, Pageable pageable);

    // ===============================================================

    // 사용자 문의 목록 조회 (본인 + 문의 + 삭제X + 제목 검색 + 상태 필터 + 페이지네이션)
    // Board만 가져오지 않고 member도 JOIN FETCH로 같이 읽어와서
    // 서비스에서 게시글마다 회원을 다시 조회하는 N+1을 줄임
    // countQuery는 페이징 전체 개수 계산용이므로 FETCH 없이 같은 조건만 유지
    @Query(value = """
          SELECT b
          FROM Board b
          JOIN FETCH b.member m
          WHERE b.brdTy = :brdTy
            AND b.brdDelYn = false
            AND m.memId = :memId
            AND (:brdInqStt IS NULL OR b.brdInqStt = :brdInqStt)
            AND (:keyword IS NULL OR b.brdTtl LIKE %:keyword%)
          ORDER BY b.brdCreDt DESC
      """,
          countQuery = """
          SELECT COUNT(b)
          FROM Board b
          JOIN b.member m
          WHERE b.brdTy = :brdTy
            AND b.brdDelYn = false
            AND m.memId = :memId
            AND (:brdInqStt IS NULL OR b.brdInqStt = :brdInqStt)
            AND (:keyword IS NULL OR b.brdTtl LIKE %:keyword%)
      """)
    Page<Board> findUserInquiryBoards(
            @Param("brdTy") BoardType brdTy,
            @Param("memId") Long memId,
            @Param("brdInqStt") InquiryStatus brdInqStt,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // 관리자 전체 문의 목록 조회(문의 + 삭제X + 제목/회사 상호명 검색 + 상태 필터 + 내 답변 여부)
    // 목록 DTO에서 회사 상호명/회원명을 바로 사용하므로 member를 JOIN FETCH로 같이 읽음
    // countQuery는 전체 개수 계산용이라 FETCH 없이 같은 조건만 유지
    @Query(value = """
          SELECT b
          FROM Board b
          JOIN FETCH b.member m
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
      """,
           countQuery = """
          SELECT COUNT(b)
          FROM Board b
          JOIN b.member m
          WHERE b.brdTy = :brdTy
            AND b.brdDelYn = false
            AND (:brdInqStt IS NULL OR b.brdInqStt = :brdInqStt)
            AND (:keyword IS NULL OR b.brdTtl LIKE %:keyword% OR m.memBizTtl LIKE %:keyword%)
      """)
    Page<Board> findAdminInquiryBoards(
            @Param("brdTy") BoardType brdTy,
            @Param("brdInqStt") InquiryStatus brdInqStt,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // 관리자가 답변한 문의 목록 조회(내가 답글을 작성한 문의 + 제목/회사 상호명 검색 + 상태 필터)
    // 목록에서 회원 정보(회원사명 등)를 바로 쓰므로 member를 JOIN FETCH로 함께 읽음
    // countQuery는 페이징 전체 건수 계산용이라 FETCH 없이 같은 조건만 유지
    @Query(value = """
          SELECT b
          FROM Board b
          JOIN FETCH b.member m
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
      """,
          countQuery = """
          SELECT COUNT(b)
          FROM Board b
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

    // 문의 목록의 부모 글 ID들로 연결된 답변들을 한 번에 조회
    // 목록 1건마다 답변을 다시 찾는 N+1을 줄이기 위한 배치 조회 메서드
    List<Board> findByBrdPrnIdInAndBrdTyAndBrdDelYnFalse(List<Long> brdPrnIds, BoardType brdTy);
}