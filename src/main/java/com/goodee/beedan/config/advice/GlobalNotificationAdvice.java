package com.goodee.beedan.config.advice;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.service.notification.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalNotificationAdvice {
    private final NotificationService notificationService;
    private final HttpServletRequest request;

    @ModelAttribute
    public void addUnreadCountToModel(Model model, Authentication authentication) {
        String uri = request.getRequestURI();
        if (uri.startsWith("/admin")) return;

        if (authentication == null || !(authentication.getPrincipal() instanceof MemberUserDetails userDetails)) {
            return;
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_ROOT"));
        if (isAdmin) return;

        try {
            Long memId = userDetails.getMemberId();
            Integer unreadCount = notificationService.getUnreadCount(memId);

            model.addAttribute("unreadCount", unreadCount);

        } catch (Exception e) {
            model.addAttribute("unreadCount", 0);
        }
    }
}