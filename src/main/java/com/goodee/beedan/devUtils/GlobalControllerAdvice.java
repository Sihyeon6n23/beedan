package com.goodee.beedan.devUtils;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.service.notification.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final NotificationService notificationService;

    public GlobalControllerAdvice(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @ModelAttribute("unreadCount")
    public int addUnreadCountToModel(@AuthenticationPrincipal MemberUserDetails userDetails) {
        if (userDetails == null) {
            return 0;
        }

        Long memId = userDetails.getMemberId();
        return notificationService.getUnreadCount(memId);
    }

}