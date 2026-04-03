package com.goodee.beedan.controller.board;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.board.InquiryBoardDetailDto;
import com.goodee.beedan.dto.board.InquiryBoardListDto;
import com.goodee.beedan.dto.board.InquiryBoardSearchDto;
import com.goodee.beedan.service.board.InquiryBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/inquiry")
public class AdminInquiryBoardController {
    private final InquiryBoardService inquiryBoardService;

    // 관리자 목록
    @GetMapping("/list")
    public String getAdminInquiryList (Model model,
                                       InquiryBoardSearchDto searchDto,
                                       @AuthenticationPrincipal MemberUserDetails userDetails) {
        searchDto.setPage(Math.max(searchDto.getPage(), 0));
        searchDto.setSize(10);
        if (searchDto.getMyAnsweredOnly() == null) {
            searchDto.setMyAnsweredOnly(false);
        }

        Page<InquiryBoardListDto> adminInquiryBoards = inquiryBoardService
                .getAdminInquiryBoards(userDetails.getMemberId(), searchDto);

        int pageBlockSize = 10;
        int totalPages = adminInquiryBoards.getTotalPages();
        int currentPage = adminInquiryBoards.getNumber();
        int startPage = totalPages > 0 ? (currentPage / pageBlockSize) * pageBlockSize : 0;
        int endPage = totalPages > 0 ? Math.min(startPage + pageBlockSize - 1, totalPages - 1) : 0;

        model.addAttribute("boardPage", adminInquiryBoards);
        model.addAttribute("searchDto", searchDto);
        model.addAttribute("isAdmin", true);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        return "/board/inquiry/inquiry-list";
    }

    // 관리자 상세
    @GetMapping("/detail")
    public String getInquiryDetail(Model model,
                                   @RequestParam("id") Long brdId,
                                   @AuthenticationPrincipal MemberUserDetails userDetails) {
        InquiryBoardDetailDto adminInquiryBoardDetail = inquiryBoardService
                .getAdminInquiryBoardDetail(brdId, userDetails.getMemberId());

        model.addAttribute("inquiryBoardDetail", adminInquiryBoardDetail);
        model.addAttribute("isAdmin", true);

        return "/board/inquiry/inquiry-detail";
    }
}
