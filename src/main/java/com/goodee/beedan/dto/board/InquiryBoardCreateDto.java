package com.goodee.beedan.dto.board;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 사용자 새 문의 작성용
public class InquiryBoardCreateDto {
    private String brdTtl;
    private String brdCon;
}
