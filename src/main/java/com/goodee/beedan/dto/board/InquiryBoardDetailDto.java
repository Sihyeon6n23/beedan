package com.goodee.beedan.dto.board;

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
// 문의 상세용 (관리자/사용자 공용)
public class InquiryBoardDetailDto {
    private Long brdId;
    private String brdTtl;
    private String brdCon;
    private InquiryStatus brdInqStt;
    private LocalDateTime brdCreDt;
    private String memBizTtl;
    private String memNm;
    private String brdCanRe;
    private Boolean canEdit;            // 사용자 문의 수정 가능 여부
    private Boolean canCancel;          // 사용자 문의 취소 가능 여부
    private Boolean canAnswer;          // 관리자 답글 작성 가능 여부
    private Boolean canUpdateStatus;    // 관리자 문의 상태 수정 가능 여부
    private Boolean canEditReply;
    private InquiryReplyDto reply;
}
