package com.goodee.beedan.controller.notice;

import com.goodee.beedan.dto.notice.NoticeDto;
import com.goodee.beedan.service.notice.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeController {
    private final NoticeService notiService;

    @GetMapping("/notification/list")
    public String getNotiList(Model model){
        List<NoticeDto> noticeDtoList = null;

        model.addAttribute("notiDtoList", noticeDtoList);

        return "notice/notice-list";
    }
}
