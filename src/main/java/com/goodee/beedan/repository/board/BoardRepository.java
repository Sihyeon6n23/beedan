package com.goodee.beedan.repository.board;

import com.goodee.beedan.common.constant.BoardType;
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
    Optional<Board> findByIdAndBoardTypeAndIsDeletedFalse(Long id, BoardType boardType);

    // 2. 공통: 삭제 안 된 게시글 페이징 목록
    Page<Board> findByBoardTypeAndIsDeletedFalse(BoardType boardType, Pageable pageable);

    // 3. 공지 특화: 상단 고정글 목록
    List<Board> findByBoardTypeAndIsFixedTrueAndIsDeletedFalse(BoardType boardType);

    // 4. 문의 특화: 내 작성글 페이징 목록
    Page<Board> findByBoardTypeAndMemberIdAndIsDeletedFalse(BoardType boardType, Long memberId, Pageable pageable);

    // 5. 공통: 조회수 증가 쿼리
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Board b SET b.viewCount = b.viewCount + 1 WHERE b.id = :id AND b.boardType = :boardType AND b.isDeleted = false")
    int increaseViewCount(@Param("id") Long id, @Param("boardType") BoardType boardType);
}
