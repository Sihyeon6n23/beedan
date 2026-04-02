package com.goodee.beedan.dto.board;

import com.goodee.beedan.common.constant.InquiryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 문의 목록 검색 조건용
public class InquiryBoardSearchDto {
    private String keyword;         // 검색용 키워드
    private InquiryStatus status;          // 문의 상태
    private Boolean myAnsweredOnly; // 내 답글 여부
    private int page;
    private int size;
}
