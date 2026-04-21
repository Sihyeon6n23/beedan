package com.goodee.beedan.config.advice;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.service.notification.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
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

        // 1. 정적 리소스 제외 (확장자 기반)
        if (uri.contains(".") && !uri.endsWith(".html")) {
            return;
        }

        // 2. 관리자 경로 및 API 요청 제외 (핵심 추가)
        // 알림 배지는 HTML 화면을 그릴 때만 필요합니다.
        if (uri.startsWith("/admin") || uri.startsWith("/api/")) {
            return;
        }

        // 3. 보안 인증 정보 확인
        if (authentication == null || !(authentication.getPrincipal() instanceof MemberUserDetails userDetails)) {
            return;
        }

        // 4. 관리자 권한 확인
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_ROOT"));
        if (isAdmin) return;

        // 로그아웃 페이지(/auth/logout) 등에서는 굳이 조회할 필요 없음
        if (uri.equals("/auth/logout")) return;

        try {
            Long memId = userDetails.getMemberId();
            Integer unreadCount = notificationService.getUnreadCount(memId);
            model.addAttribute("unreadCount", unreadCount);
        } catch (Exception e) {
            model.addAttribute("unreadCount", 0);
        }
    }


}