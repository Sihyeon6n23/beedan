package com.goodee.beedan.dto.board.notice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardResultResponseDto {
    private String actionMessage;
    private BoardResultMessage boardResultMessage;
    private Long targetId;
}
