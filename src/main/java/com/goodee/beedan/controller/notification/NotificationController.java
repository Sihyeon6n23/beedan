package com.goodee.beedan.controller.notification;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.notification.NotificationDto;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/notification")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/list")
    public String getNotiList(Model model, @AuthenticationPrincipal MemberUserDetails userDetails){
        if(userDetails == null){
            return "redirect:/auth/signin";
        }

        List<NotificationDto> notificationDtoList = notificationService.getUnReadNotificationList(userDetails.getMemberId());
        model.addAttribute("notifications", notificationDtoList);

        return "notification/notification-list";
    }
}
