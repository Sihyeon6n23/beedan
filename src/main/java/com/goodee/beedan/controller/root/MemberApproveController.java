package com.goodee.beedan.controller.root;

import com.goodee.beedan.common.constant.BoardType;
import com.goodee.beedan.common.constant.SearchType;
import com.goodee.beedan.dto.board.notice.PageResponseDto;
import com.goodee.beedan.dto.board.notice.SearchDto;
import com.goodee.beedan.dto.member.MemberApproveDto; // 필요에 따라 PageResponse 등으로 래핑 가능
import com.goodee.beedan.service.member.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/root/member") // 뷰 템플릿의 actionUrl과 일치시킴
@RequiredArgsConstructor
@Slf4j
public class MemberApproveController {

    private final MemberService memberService;

    /**
     * 가입 승인 대기 목록 조회 (검색 및 페이징 포함)
     */
    @GetMapping("/approve")
    public String approvePage(@Valid @ModelAttribute("searchDto") SearchDto searchDto,
                              BindingResult bindingResult,
                              Model model) {

        try {
            if (bindingResult.hasErrors()) {
                searchDto = new SearchDto();
            }

            PageResponseDto<MemberApproveDto> response = memberService.getPendingMemberList(searchDto);

            model.addAttribute("members", response.getContent());
            model.addAttribute("response", response);
            model.addAttribute("searchDto", searchDto);
            model.addAttribute("boardUri", "/root/member/approve");
            model.addAttribute("searchTypes", SearchType.getSupportedTypes(BoardType.APPROVE));

            return "root/member/approve"; // 정상 작동 시 HTML 반환

        } catch (Exception e) {
            // ✨ 에러가 발생하면 무조건 콘솔에 빨간 글씨로 출력하게 만듭니다.
            e.printStackTrace();
            log.error("approvePage 접속 중 에러 발생: ", e);
            throw e;
        }
    }
}