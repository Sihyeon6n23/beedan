package com.goodee.beedan.devUtils;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.service.notification.NotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@ControllerAdvice
public class GlobalControllerAdvice {

    private final NotificationService notificationService;

    public GlobalControllerAdvice(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @ModelAttribute("unreadCount")
    public int addUnreadCountToModel(@AuthenticationPrincipal MemberUserDetails userDetails) {
        if (userDetails == null) return 0;

        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
                || userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ROOT"));

        if (isAdmin) return 0;

        return notificationService.getUnreadCount(userDetails.getMemberId());
    }

}