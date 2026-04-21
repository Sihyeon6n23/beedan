package com.goodee.beedan.controller.board;

import com.goodee.beedan.common.constant.BoardType;
import com.goodee.beedan.common.constant.SearchType;
import com.goodee.beedan.dto.board.notice.*;
import com.goodee.beedan.dto.file.FileDto;
import com.goodee.beedan.dto.file.RefDto;
import com.goodee.beedan.repository.file.FileRepository;
import com.goodee.beedan.service.board.NoticeBoardService;
import com.goodee.beedan.service.board.ViewCountService;
import com.goodee.beedan.service.file.FileService;
import com.goodee.beedan.service.file.FileUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
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
    private final FileUtils fileUtils;
    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSizeStr;
    @Value("${spring.servlet.multipart.max-request-size}")
    private String maxRequestSizeStr;


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

        model.addAttribute("boardUri", "/notice/list");    // 고정글 + 페이징 결과 포함됨
        model.addAttribute("response", response);    // 고정글 + 페이징 결과 포함됨
        model.addAttribute("searchDto", searchDto);
        model.addAttribute("searchTypes", SearchType.getSupportedTypes(BoardType.NOTICE));

        return "board/notice/notice-list";
    }

    /** 2. 상세 조회 (조회수 중복 방지 및 통합 DTO 반영) */
    @GetMapping("/detail")
    public String noticeDetail(@RequestParam("id") Long id,
                               @RequestParam(value = "returnUrl", required = false) String returnUrl,
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
        model.addAttribute("returnUrl", returnUrl);


        return "board/notice/notice-detail";
    }

    /** 3. 작성/수정 폼 이동 (Common DTO 규격 반영) */
    @GetMapping({"/write", "/edit"})
    public String noticeForm(@RequestParam(value = "id", required = false) Long id,
                             Principal principal,
                             Model model) {
        long maxFileSize = fileUtils.parseSize(maxFileSizeStr);
        long maxRequestSize = fileUtils.parseSize(maxRequestSizeStr);

        if (id != null) {
            CommonBoardDetailDto detail = noticeBoardService.getNoticeDetail(id, principal.getName());

            CommonBoardRequestDto requestDto = CommonBoardRequestDto.builder()
                    .brdId(detail.getBrdId())
                    .brdTtl(detail.getBrdTtl())
                    .brdCon(detail.getBrdCon())
                    .brdFixYn(detail.getBrdFixYn())
                    .build();

            model.addAttribute("boardRequestDto", requestDto); // 수정 폼에는 RequestDto를 넘김
            model.addAttribute("fileList", detail.getFileList()); // 기존 파일 목록은 따로 넘김
            model.addAttribute("isEdit", true);
        } else {
            // 등록 모드
            model.addAttribute("boardRequestDto", new CommonBoardRequestDto());
            model.addAttribute("isEdit", false);
        }
        model.addAttribute("maxFileSize", maxFileSize);
        model.addAttribute("maxRequestSize", maxRequestSize);

        return "board/notice/notice-write";
    }

    /** 4. 작성 실행 (CommonBoardRequestDto 사용) */
    @PostMapping("/write")
    public String postWrite(@Valid @ModelAttribute("boardRequestDto") CommonBoardRequestDto boardRequestDto,
                            BindingResult bindingResult,
                            Principal principal,
                            RedirectAttributes reAttr,
                            Model model) throws IOException {
        if (!fileService.validateFileCount(boardRequestDto.getNewFiles(), 0, 0, 5L)) {
            bindingResult.rejectValue("newFiles", "fileInvalidCount", "파일 업로드 개수를 초과했습니다.");
            log.info("파일 업로드 개수를 초과했습니다.");
            model.addAttribute("boardRequestDto", boardRequestDto);
            model.addAttribute("isEdit", false);
            return "board/notice/notice-write";
        }

        String boardResultMessage = noticeBoardService.writeNotice(boardRequestDto, principal.getName());

        model.addAttribute("boardRequestDto", new CommonBoardRequestDto());
        model.addAttribute("isEdit", false);
        model.addAttribute("noticeSuccess", true);
        model.addAttribute("noticeSuccessMessage",
                "공지사항이 등록되었습니다." +
                        (boardResultMessage != null ?
                                boardResultMessage :
                                ""));
        model.addAttribute("noticeSuccessRedirect", "/notice/list");
        return "board/notice/notice-write";
    }

    /** 5. 수정 실행 (CommonBoardRequestDto 사용) */
    @PostMapping("/edit")
    public String postUpdate(@Valid @ModelAttribute("boardRequestDto") CommonBoardRequestDto boardRequestDto,
                             BindingResult bindingResult,
                             Principal principal,
                             RedirectAttributes reAttr,
                             Model model) throws IOException {
        Long brdId = boardRequestDto.getBrdId();
        long deleteCount = (boardRequestDto.getDeleteUuids() != null) ? boardRequestDto.getDeleteUuids().size() : 0L;

        CommonBoardDetailDto detail = noticeBoardService.getNoticeDetail(brdId, principal.getName());
        List<FileDto> fileList = (detail != null) ? detail.getFileList() : new ArrayList<>();
        long existCount = fileList.size();

        if (!fileService.validateFileCount(boardRequestDto.getNewFiles(), existCount, deleteCount, 5L)) {
            bindingResult.rejectValue("newFiles", "fileInvalidCount", "파일 업로드 개수를 초과했습니다.");

            // 에러 발생 시 View에 필요한 데이터 재입력
            model.addAttribute("boardRequestDto", boardRequestDto);
            model.addAttribute("fileList", fileList);
            model.addAttribute("isEdit", true);

            return "board/notice/notice-write";
        }

        String boardResultMessage = noticeBoardService.updateNotice(boardRequestDto, principal.getName());

        model.addAttribute("boardRequestDto", boardRequestDto);
        model.addAttribute("fileList", fileList);
        model.addAttribute("isEdit", true);
        model.addAttribute("noticeSuccess", true);
        model.addAttribute("noticeSuccessMessage",
                "공지사항이 수정되었습니다." +
                        (boardResultMessage != null ?
                                boardResultMessage :
                                ""));
        model.addAttribute("noticeSuccessRedirect", "/notice/detail?id=" + boardRequestDto.getBrdId());
        return "board/notice/notice-write";
    }

    /** 6. 삭제 실행 */
    @PostMapping("/delete")
    public String delete(@RequestParam("brdId") Long brdId, Principal principal) {
        noticeBoardService.deleteNotice(brdId, principal.getName());
        return "redirect:/notice/list";
    }

}