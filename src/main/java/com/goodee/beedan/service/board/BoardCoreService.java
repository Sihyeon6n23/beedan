package com.goodee.beedan.service.board;

import com.goodee.beedan.common.constant.BoardType;
import com.goodee.beedan.entity.Board;
import com.goodee.beedan.repository.board.BoardRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BoardCoreService {
    private final BoardRepository boardRepository;

    public Board getBoard(Long boardId, BoardType boardType) {
        return boardRepository.findByBrdIdAndBrdTyAndBrdDelYnFalse(boardId, boardType)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
    }

    public void increaseViewCount(Long boardId, BoardType boardType) {
        int updatedCount = boardRepository.increaseViewCount(boardId, boardType);

        if (updatedCount == 0) {
            log.warn("조회수 증가 실패 - 존재하지 않거나 삭제된 게시글입니다.");
        }
    }

    public void softDeleteBoard(Long boardId, BoardType boardType) {
        Board board = getBoard(boardId, boardType);
        board.markAsDeleted();

        log.info("게시글 삭제 완료 - 게시글 정보 {}", boardId + ":" + boardType);
    }

    public Page<Board> getBoardList(BoardType boardType, Pageable pageable) {
        Page<Board> boardPage = boardRepository.findByBrdTyAndBrdDelYnFalse(boardType, pageable);
        if (boardPage == null) {
            log.info("{} 게시글이 0건 입니다.", boardType);
        }

        return boardPage;
    }


}
