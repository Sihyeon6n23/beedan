package com.goodee.beedan.controller.board;

import com.goodee.beedan.dto.board.notice.NoticeBoardRequestDto;
import com.goodee.beedan.dto.board.notice.NoticeDetailDto;
import com.goodee.beedan.service.board.NoticeBoardService;
import com.goodee.beedan.service.board.ViewCountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/notice")
@Slf4j
public class NoticeBoardController {

    private final NoticeBoardService noticeBoardService;
    private final ViewCountService viewCountService;

    /** 1. 목록 조회 */
    @GetMapping("/list")
    public String noticeList(Model model,
                             @PageableDefault(size = 10, sort = "brdId", direction = Sort.Direction.DESC) Pageable pageable) {

        // 서비스에서 고정글(fixedNotices)과 일반글(notices)을 Map으로 받아옴
        Map<String, Object> result = noticeBoardService.getNoticeList(pageable);

        model.addAttribute("fixedNotices", result.get("fixedNotices"));
        model.addAttribute("boardPage", result.get("notices")); // 뷰의 변수명과 일치시킴

        return "board/notice/notice-list";
    }

    /** 2. 상세 조회 (조회수 중복 방지 포함) */
    @GetMapping("/detail")
    public String noticeDetail(@RequestParam("id") Long id,
                               HttpServletRequest request,
                               HttpServletResponse response,
                               Principal principal,
                               Model model) {

        // 조회수 증가 로직 (쿠키)
        viewCountService.increaseViewCountWithCookie(id, request, response);

        // 상세 데이터 조회 (이전/다음글, 파일목록 포함)
        String username = (principal != null) ? principal.getName() : null;
        NoticeDetailDto notice = noticeBoardService.getNoticeDetail(id, username);

        model.addAttribute("notice", notice);
        model.addAttribute("fileList", notice.getFileList());
        model.addAttribute("canModify", notice.isCanModify());

        return "board/notice/notice-detail";
    }

    /** 3. 작성/수정 폼 이동 (통합) */
    @GetMapping({"/write", "/edit"})
    public String noticeForm(@RequestParam(value = "id", required = false) Long id,
                             Principal principal,
                             Model model) {
        if (id != null) {
            // 수정 모드
            NoticeDetailDto notice = noticeBoardService.getNoticeDetail(id, principal.getName());
            model.addAttribute("boardCreateDto", notice); // 수정 시 기존 데이터 바인딩
            model.addAttribute("fileList", notice.getFileList());
            model.addAttribute("isEdit", true);
        } else {
            // 등록 모드
            model.addAttribute("boardCreateDto", new NoticeBoardRequestDto());
            model.addAttribute("isEdit", false);
        }

        return "board/notice/notice-write";
    }

    /** 4. 작성 실행 */
    @PostMapping("/write")
    public String postWrite(@ModelAttribute NoticeBoardRequestDto boardCreateDto,
                            Principal principal) throws IOException {

        noticeBoardService.writeNotice(boardCreateDto, principal.getName());
        return "redirect:/notice/list";
    }

    /** 5. 수정 실행 */
    @PostMapping("/edit")
    public String postUpdate(@ModelAttribute NoticeBoardRequestDto boardUpdateDto,
                             Principal principal) throws IOException {

        noticeBoardService.updateNotice(boardUpdateDto, principal.getName());
        return "redirect:/notice/detail?id=" + boardUpdateDto.getBrdId();
    }

    /** 6. 삭제 실행 */
    @PostMapping("/delete")
    public String delete(@RequestParam("brdId") Long brdId, Principal principal) {
        noticeBoardService.deleteNotice(brdId, principal.getName());
        return "redirect:/notice/list";
    }
}
