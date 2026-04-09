package com.goodee.beedan.controller.board;

import com.goodee.beedan.common.constant.InquiryStatus;
import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.board.inquiry.InquiryBoardDetailDto;
import com.goodee.beedan.dto.board.inquiry.InquiryBoardListDto;
import com.goodee.beedan.dto.board.inquiry.InquiryBoardSearchDto;
import com.goodee.beedan.dto.board.inquiry.InquiryReplyDto;
import com.goodee.beedan.dto.board.inquiry.InquiryReplySaveDto;
import com.goodee.beedan.service.board.InquiryBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/inquiries")
public class AdminInquiryBoardRestController {
    private final InquiryBoardService inquiryBoardService;

    // 답글 조회
    @GetMapping("/{id}/reply")
    public ResponseEntity<InquiryReplyDto> getInquiryReply(@PathVariable("id") Long brdId,
                                                           @AuthenticationPrincipal MemberUserDetails userDetails) {
        InquiryBoardDetailDto inquiryBoardDetail = inquiryBoardService
                .getAdminInquiryBoardDetail(brdId, userDetails.getMemberId());

        return ResponseEntity.ok(inquiryBoardDetail.getReply());
    }

    // 관리자 답글 작성
    @PostMapping("/{id}/reply")
    public ResponseEntity<Long> createInquiryReply(@PathVariable("id") Long brdId,
                                                   @AuthenticationPrincipal MemberUserDetails userDetails,
                                                   @ModelAttribute InquiryReplySaveDto inquiryReplySaveDto) throws IOException {
        Long replyId = inquiryBoardService.createInquiryReply(
                brdId,
                userDetails.getMemberId(),
                inquiryReplySaveDto
        );
        return ResponseEntity.ok(replyId);
    }

    // 관리자 답글 수정
    @PostMapping("/{id}/reply/edit")
    public ResponseEntity<Void> updateInquiryReply(@PathVariable("id") Long brdId,
                                                   @AuthenticationPrincipal MemberUserDetails userDetails,
                                                   @ModelAttribute InquiryReplySaveDto inquiryReplySaveDto) throws IOException {
        inquiryBoardService.updateInquiryReply(
                brdId,
                userDetails.getMemberId(),
                inquiryReplySaveDto
        );
        return ResponseEntity.ok().build();
    }

    // 관리자 문의 상태 수정
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateInquiryStatus(@PathVariable("id") Long brdId,
                                                    @AuthenticationPrincipal MemberUserDetails userDetails,
                                                    @RequestParam("status") InquiryStatus inquiryStatus,
                                                    @RequestParam(value = "brdCanRe", required = false) String brdCanRe) {
        inquiryBoardService.updateInquiryStatus(brdId, userDetails.getMemberId(), inquiryStatus, brdCanRe);
        return ResponseEntity.ok().build();
    }

    // 관리자 문의 목록 조회
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