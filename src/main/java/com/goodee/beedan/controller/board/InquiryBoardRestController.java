package com.goodee.beedan.controller.board;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.board.inquiry.InquiryBoardDetailDto;
import com.goodee.beedan.dto.board.inquiry.InquiryReplyDto;
import com.goodee.beedan.service.board.InquiryBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inquiries")
public class InquiryBoardRestController {
    private final InquiryBoardService inquiryBoardService;

    // 답글 조회
    @GetMapping("/{id}/reply")
    public ResponseEntity<InquiryReplyDto> getInquiryReply(@PathVariable("id") Long brdId,
                                                           @AuthenticationPrincipal MemberUserDetails userDetails) {
        InquiryBoardDetailDto inquiryBoardDetail = inquiryBoardService
                .getUserInquiryBoardDetail(brdId, userDetails.getMemberId());

        return ResponseEntity.ok(inquiryBoardDetail.getReply());
    }

    // 사용자 문의 취소
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelInquiry(@PathVariable("id") Long brdId,
                                              @AuthenticationPrincipal MemberUserDetails userDetails) {
        inquiryBoardService.cancelInquiryBoard(brdId, userDetails.getMemberId());
        return ResponseEntity.ok().build();
    }
}