package com.goodee.beedan.controller.board;

import com.goodee.beedan.common.constant.InquiryStatus;
import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.board.inquiry.*;
import com.goodee.beedan.service.board.InquiryBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

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
                                   @ModelAttribute InquiryReplySaveDto inquiryReplySaveDto) throws IOException {
        return inquiryBoardService.createInquiryReply(
                brdId,
                userDetails.getMemberId(),
                inquiryReplySaveDto
        );
    }

    // 관리자 답글 수정
    @PostMapping("/{id}/reply/edit")
    public void updateInquiryReply(@PathVariable("id") Long brdId, // 답글 Id
                                   @AuthenticationPrincipal MemberUserDetails userDetails,
                                   @ModelAttribute InquiryReplySaveDto inquiryReplySaveDto) throws IOException {
        inquiryBoardService.updateInquiryReply(
                brdId,
                userDetails.getMemberId(),
                inquiryReplySaveDto
        );
    }

    // 관리자 문의 상태 수정
    @PatchMapping("/{id}/status")
    public void updateInquiryStatus(@PathVariable("id") Long brdId,
                                    @AuthenticationPrincipal MemberUserDetails userDetails,
                                    @RequestParam("status") InquiryStatus inquiryStatus,
                                    @RequestParam(value = "brdCanRe", required = false) String brdCanRe) {
        inquiryBoardService.updateInquiryStatus(brdId, userDetails.getMemberId(), inquiryStatus, brdCanRe);
    }

    @GetMapping("{id}")
    public ResponseEntity<Page<InquiryBoardListDto>> getInquiryList(@PathVariable("id") Long memId,
                                                                    @RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "10") int size) {
        InquiryBoardSearchDto searchDto = InquiryBoardSearchDto.builder()
                .page(page)
                .size(size)
                .build();
        Page<InquiryBoardListDto> userInquiryBoards = inquiryBoardService.getUserInquiryBoards(memId, searchDto);

        return ResponseEntity.ok(userInquiryBoards);
    }

}
