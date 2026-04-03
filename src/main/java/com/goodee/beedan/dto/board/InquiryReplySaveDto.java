package com.goodee.beedan.dto.board;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 관리자 답변 작성/수정용
public class InquiryReplySaveDto {
    private String brdCon;
}
