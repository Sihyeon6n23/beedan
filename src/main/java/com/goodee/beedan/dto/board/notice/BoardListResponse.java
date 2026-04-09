package com.goodee.beedan.dto.board.notice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardListResponse {
    // 공지사항 등에서 상단에 고정될 글들
    private List<CommonBoardDetailDto> fixedBoards;

    // 페이징 처리된 일반 게시글들
    private PageResponseDto<CommonBoardDetailDto> pagination;

    public static BoardListResponse of(List<CommonBoardDetailDto> fixed, PageResponseDto<CommonBoardDetailDto> normal) {
        return BoardListResponse.builder()
                .fixedBoards(fixed)
                .pagination(normal)
                .build();
    }
}
