package com.goodee.beedan.dto.board.inquiry;

import com.goodee.beedan.common.constant.InquiryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 문의 목록용 (관리자/사용자 공용)
public class InquiryBoardListDto {
    private Long brdId;                 // 글 id
    private String brdTtl;              // 글 제목
    private InquiryStatus brdInqStt;    // 문의 상태
    private LocalDateTime brdCreDt;     // 글 작성 일시
    private String memBizTtl;           // 회사 상호명
    private String memNm;               // 작성자(관리자/사용자) 이름
    private Boolean hasReply;           // 답글 존재 여부
    private Boolean replyEdited;        // 답글 수정 여부
    private Boolean hasAttachment;      // 문의 원글 첨부 존재 여부
}
