package com.goodee.beedan.dto.board;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 사용자 기존 문의 수정용
public class InquiryBoardEditDto {
    private String brdTtl;
    private String brdCon;
}
