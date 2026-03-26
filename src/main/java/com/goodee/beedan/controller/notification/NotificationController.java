package com.goodee.beedan.controller.notification;

import com.goodee.beedan.dto.notification.NotificationDto;
import com.goodee.beedan.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NotificationController {
    private final NotificationService notiService;

    @GetMapping("/notification/list")
    public String getNotiList(Model model){
        List<NotificationDto> notificationDtoList = null;

        model.addAttribute("notiDtoList", notificationDtoList);

        return "notice/notice-list";
    }
}
