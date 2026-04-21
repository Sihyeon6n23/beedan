package com.goodee.beedan.dto.board.notice;

import com.goodee.beedan.common.constant.InquiryStatus;
import com.goodee.beedan.dto.board.inquiry.InquiryReplyDto;
import com.goodee.beedan.dto.file.FileDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonBoardDetailDto {
    // --- [공통 필드] 모든 게시판 사용 ---
    private Long brdId;
    private String brdTtl;
    private String brdCon;
    private Long brdHit;
    private LocalDateTime brdCreDt;
    private String memNm;
    private Long memId;
    private List<FileDto> fileList;

    // --- [공지사항 특화] ---
    private Boolean brdFixYn;
    private CommonBoardDetailDto prevBoard; // 이전글
    private CommonBoardDetailDto nextBoard; // 다음글

    // --- [문의게시판 특화] ---
    private InquiryStatus brdInqStt;  // 문의 상태
    private String memBizTtl;         // 상호명
    private String brdCanRe;          // 취소/반려 사유
    private InquiryReplyDto reply;    // 답변 객체

    // --- [권한/상태 플래그] ---
    private Boolean canEdit;          // 수정 가능 여부
    private Boolean canCancel;        // 취소 가능 여부
    private Boolean canAnswer;        // 답변 가능 여부
    private Boolean canUpdateStatus;  // [추가] 관리자 상태 변경 권장 여부

    // --- [목록 UI 전용 유틸리티 필드] ---
    private Boolean fileYn;           // 목록에서 📎 아이콘 표시 여부 (DB 조회 최적화용)
    private Boolean isNew;            // 24시간 이내 작성 여부 (New 배지 표시용)
    private Boolean replyEdited;      // 답글 수정 여부 (InquiryBoardListDto의 필드)
    private Integer commentCount;     // 자유게시판 등에서 사용될 댓글 수 (확장성)
}