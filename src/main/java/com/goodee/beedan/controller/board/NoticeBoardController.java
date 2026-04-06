package com.goodee.beedan.controller.board;

import com.goodee.beedan.dto.board.notice.NoticeBoardCreateDto;
import com.goodee.beedan.dto.file.RefDto;
import com.goodee.beedan.service.board.NoticeBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeBoardController {
    private final NoticeBoardService noticeBoardService;

    @GetMapping("/write")
    public String getWrite(Model model) {
        model.addAttribute("boardCreateDto", new NoticeBoardCreateDto());

        return "board/notice/notice-write";
    }
    @PostMapping("/write")
    public String postWrite(@ModelAttribute NoticeBoardCreateDto boardCreateDto) {
        RefDto refDto = RefDto.builder()
                .refTy("NOTICE")
                .build();
        noticeBoardService.writeNotice(boardCreateDto, refDto);

        return "board/notice/notice-write";
    }
}
