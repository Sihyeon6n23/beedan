package com.goodee.beedan.controller.board;

import com.goodee.beedan.common.constant.BoardType;
import com.goodee.beedan.dto.board.notice.*;
import com.goodee.beedan.dto.file.RefDto;
import com.goodee.beedan.service.board.NoticeBoardService;
import com.goodee.beedan.service.board.ViewCountService;
import com.goodee.beedan.service.file.FileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/notice")
@Slf4j
public class NoticeBoardController {

    private final NoticeBoardService noticeBoardService;
    private final ViewCountService viewCountService;
    private final FileService fileService;

    @ModelAttribute("searchTypes")
    public Map<String, String> searchTypes() {
        Map<String, String> types = new LinkedHashMap<>();
        types.put("all", "전체");
        types.put("title", "제목");
        types.put("content", "내용");
        types.put("writer", "작성자");
        return types;
    }

    /** 1. 목록 조회 (통합 DTO 및 BoardListResponse 반영) */
    @GetMapping("/list")
    public String getNoticeList(@Valid @ModelAttribute("searchDto") SearchDto searchDto,
                                BindingResult bindingResult, Model model) {

        // 방어적 프로그래밍: 입력값 오류 시 초기화
        if (bindingResult.hasErrors()) {
            searchDto = new SearchDto();
        }

        // 서비스 호출 (BoardType.NOTICE 명시)
        BoardListResponse response = noticeBoardService.getBoardList(BoardType.NOTICE, searchDto);

        model.addAttribute("response", response);    // 고정글 + 페이징 결과 포함됨
        model.addAttribute("searchDto", searchDto);

        return "board/notice/notice-list";
    }

    /** 2. 상세 조회 (조회수 중복 방지 및 통합 DTO 반영) */
    @GetMapping("/detail")
    public String noticeDetail(@RequestParam("id") Long id,
                               HttpServletRequest request,
                               HttpServletResponse response,
                               Principal principal,
                               Model model) {

        // 조회수 증가 로직 (쿠키 기반)
        viewCountService.increaseViewCountWithCookie(id, request, response);

        // 상세 데이터 조회 (CommonBoardDetailDto 사용)
        String username = (principal != null) ? principal.getName() : null;
        CommonBoardDetailDto notice = noticeBoardService.getNoticeDetail(id, username);

        model.addAttribute("notice", notice);

        return "board/notice/notice-detail";
    }

    /** 3. 작성/수정 폼 이동 (Common DTO 규격 반영) */
    @GetMapping({"/write", "/edit"})
    public String noticeForm(@RequestParam(value = "id", required = false) Long id,
                             Principal principal,
                             Model model) {

        if (id != null) {
            // 수정 모드: 상세 조회 DTO를 가져와서 Request DTO로 변환하여 전달하거나 모델에 직접 매핑
            CommonBoardDetailDto detail = noticeBoardService.getNoticeDetail(id, principal.getName());
            model.addAttribute("boardRequestDto", detail);
            model.addAttribute("isEdit", true);
        } else {
            // 등록 모드
            model.addAttribute("boardRequestDto", new CommonBoardRequestDto());
            model.addAttribute("isEdit", false);
        }

        return "board/notice/notice-write";
    }

    /** 4. 작성 실행 (CommonBoardRequestDto 사용) */
    @PostMapping("/write")
    public String postWrite(@ModelAttribute CommonBoardRequestDto boardRequestDto,
                            Principal principal) throws IOException {

        noticeBoardService.writeNotice(boardRequestDto, principal.getName());
        return "redirect:/notice/list";
    }

    /** 5. 수정 실행 (CommonBoardRequestDto 사용) */
    @PostMapping("/edit")
    public String postUpdate(@ModelAttribute CommonBoardRequestDto boardRequestDto,
                             Principal principal) throws IOException {

        noticeBoardService.updateNotice(boardRequestDto, principal.getName());
        return "redirect:/notice/detail?id=" + boardRequestDto.getBrdId();
    }

    /** 6. 삭제 실행 */
    @PostMapping("/delete")
    public String delete(@RequestParam("brdId") Long brdId, Principal principal) {
        noticeBoardService.deleteNotice(brdId, principal.getName());
        return "redirect:/notice/list";
    }
}