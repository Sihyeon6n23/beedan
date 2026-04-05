package com.goodee.beedan.controller.board;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.board.*;
import com.goodee.beedan.service.board.InquiryBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/inquiry")
public class InquiryBoardController {
    private final InquiryBoardService inquiryBoardService;

    // 사용자 목록
    @GetMapping("/list")
    public String getInquiryList(Model model,
                       InquiryBoardSearchDto searchDto,
                       @AuthenticationPrincipal MemberUserDetails userDetails) {
        searchDto.setPage(Math.max(searchDto.getPage(), 0));
        searchDto.setSize(10);

        Page<InquiryBoardListDto> userInquiryBoards = inquiryBoardService
                .getUserInquiryBoards(userDetails.getMemberId(), searchDto);

        int pageBlockSize = 10;
        int totalPages = userInquiryBoards.getTotalPages();
        int currentPage = userInquiryBoards.getNumber();
        int startPage = totalPages > 0 ? (currentPage / pageBlockSize) * pageBlockSize : 0;
        int endPage = totalPages > 0 ? Math.min(startPage + pageBlockSize - 1, totalPages - 1) : 0;

        model.addAttribute("boardPage", userInquiryBoards);
        model.addAttribute("searchDto", searchDto);
        model.addAttribute("isAdmin", false);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "/board/inquiry/inquiry-list";
    }

    // 사용자 상세
    @GetMapping("/detail")
    public String getInquiryDetail(Model model,
                         @RequestParam("id") Long brdId,
                         @AuthenticationPrincipal MemberUserDetails userDetails) {
        InquiryBoardDetailDto inquiryBoardDetail = inquiryBoardService
                .getUserInquiryBoardDetail(brdId, userDetails.getMemberId());

        model.addAttribute("inquiryBoardDetail", inquiryBoardDetail);
        model.addAttribute("isAdmin", false);

        return "/board/inquiry/inquiry-detail";
    }

    // 사용자 문의 작성 화면
    @GetMapping("/write")
    public String writeInquiry(Model model) {
        model.addAttribute("inquiryBoardCreateDto", new InquiryBoardCreateDto());

        return "/board/inquiry/inquiry-write";
    }

    // 사용자 문의 작성 처리
    @PostMapping("/write")
    public String writeInquiry(@ModelAttribute InquiryBoardCreateDto inquiryBoardCreateDto,
                               @AuthenticationPrincipal MemberUserDetails userDetails) {
        Long brdId = inquiryBoardService
                .createInquiryBoard(userDetails.getMemberId(), inquiryBoardCreateDto);

        return "redirect:/inquiry/detail?id=" + brdId;
    }

    // 사용자 문의 수정 화면
    @GetMapping("/edit")
    public String editInquiry(Model model,
                       @RequestParam("id") Long brdId,
                       @AuthenticationPrincipal MemberUserDetails userDetails) {
        InquiryBoardDetailDto inquiryBoardDetail = inquiryBoardService
                .getUserInquiryBoardDetail(brdId, userDetails.getMemberId());

        InquiryBoardEditDto inquiryBoardEditDto = InquiryBoardEditDto.builder()
                .brdTtl(inquiryBoardDetail.getBrdTtl())
                .brdCon(inquiryBoardDetail.getBrdCon())
                .build();

        model.addAttribute("brdId", brdId);
        model.addAttribute("inquiryBoardEditDto", inquiryBoardEditDto);

        return "/board/inquiry/inquiry-edit";
    }

    // 사용자 문의 수정 처리
    @PostMapping("/edit")
    public String editInquiry(@RequestParam("id") Long brdId,
                              @ModelAttribute InquiryBoardEditDto inquiryBoardEditDto,
                              @AuthenticationPrincipal MemberUserDetails userDetails) {
        inquiryBoardService.updateInquiryBoard(brdId, userDetails.getMemberId(), inquiryBoardEditDto);

        return "redirect:/inquiry/detail?id=" + brdId;
    }
}
