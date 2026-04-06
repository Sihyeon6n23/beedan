package com.goodee.beedan.service.board;

import com.goodee.beedan.common.constant.BoardType;
import com.goodee.beedan.dto.board.notice.NoticeBoardCreateDto;
import com.goodee.beedan.dto.file.RefDto;
import com.goodee.beedan.entity.Board;
import com.goodee.beedan.repository.board.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NoticeBoardService {
    private final BoardRepository boardRepository;
    public void writeNotice(NoticeBoardCreateDto boardCreateDto, RefDto refDto) {
        Board board = Board.builder()
                .brdTy(BoardType.valueOf(refDto.getRefTy()))
                .brdTtl(boardCreateDto.getBrdTtl())
                .brdCon(boardCreateDto.getBrdCon())
                .brdFixYn(boardCreateDto.getBrdFixYn())
                .brdCreDt(LocalDateTime.now())
                .brdVstCnt(0L)
                .brdDelYn(false)
                //.member()
                .build();
        boardRepository.save(board);

    }
}
