package com.goodee.beedan.controller.board;

import com.goodee.beedan.common.constant.InquiryStatus;
import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.board.InquiryBoardDetailDto;
import com.goodee.beedan.dto.board.InquiryReplyDto;
import com.goodee.beedan.dto.board.InquiryReplySaveDto;
import com.goodee.beedan.service.board.InquiryBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/inquiries")
public class AdminInquiryBoardRestController {
    private final InquiryBoardService inquiryBoardService;

    // 답글 조회
    @GetMapping("/{id}/reply")
    public InquiryReplyDto getInquiryReply(@PathVariable("id") Long brdId,
                                           @AuthenticationPrincipal MemberUserDetails userDetails) {
        InquiryBoardDetailDto inquiryBoardDetail = inquiryBoardService
                .getAdminInquiryBoardDetail(brdId, userDetails.getMemberId());

        return inquiryBoardDetail.getReply();
    }
    
    // 관리자 답글 작성
    @PostMapping("/{id}/reply")
    public Long createInquiryReply(@PathVariable("id") Long brdId, // 문의글 Id
                                   @AuthenticationPrincipal MemberUserDetails userDetails,
                                   @RequestBody InquiryReplySaveDto inquiryReplySaveDto) {
        return inquiryBoardService.createInquiryReply(
                brdId,
                userDetails.getMemberId(),
                inquiryReplySaveDto
        );
    }
    
    // 관리자 답글 수정
    @PatchMapping("/{id}/reply")
    public void updateInquiryReply(@PathVariable("id") Long brdId, // 답글 Id
                                   @AuthenticationPrincipal MemberUserDetails userDetails,
                                   @RequestBody InquiryReplySaveDto inquiryReplySaveDto) {
        inquiryBoardService.updateInquiryReply(
                brdId,
                userDetails.getMemberId(),
                inquiryReplySaveDto
        );
    }
    
    // 관리자 문의 상태 수정
    @PatchMapping("/{id}/status")
    public void updateInquiryStatus(@PathVariable("id") Long brdId,
                                    @RequestParam("status") InquiryStatus inquiryStatus,
                                    @RequestParam(value = "brdCanRe", required = false) String brdCanRe) {
        inquiryBoardService.updateInquiryStatus(brdId, inquiryStatus, brdCanRe);
    }
}
