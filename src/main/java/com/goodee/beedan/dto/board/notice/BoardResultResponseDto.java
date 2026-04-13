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
    private BoardResultMessage boardResultMessage;
    private Long targetId;
}
