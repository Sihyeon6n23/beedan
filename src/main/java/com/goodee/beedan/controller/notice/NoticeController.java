package com.goodee.beedan.controller.notice;

import com.goodee.beedan.dto.notice.NotiDto;
import com.goodee.beedan.service.notice.NotiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class NoticeController {
    private final NotiService notiService;

    @GetMapping("/notification/list")
    public String getNotiList(Model model){
        List<NotiDto> notiDtoList = null;

        model.addAttribute("notiDtoList", notiDtoList);

        return "notice/notice-list";
    }
}
