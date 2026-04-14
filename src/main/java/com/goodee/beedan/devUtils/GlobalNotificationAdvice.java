package com.goodee.beedan.devUtils;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.service.notification.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.security.core.Authentication;

@ControllerAdvice
public class GlobalNotificationAdvice {

    private final NotificationService notificationService;

    public GlobalNotificationAdvice(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @ModelAttribute
    public void addUnreadCountToSession(HttpSession session, Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof MemberUserDetails userDetails) {
            Long memId = userDetails.getMemberId();

            Integer unreadCount = notificationService.getUnreadCount(memId);

            session.setAttribute("unreadCount", unreadCount);
        }
    }
}