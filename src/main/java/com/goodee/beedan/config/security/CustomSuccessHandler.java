package com.goodee.beedan.config.security;

import com.goodee.beedan.dto.root.security.SecurityPolicyDto;
import com.goodee.beedan.repository.sessionlog.SessionLogRepository;
import com.goodee.beedan.service.member.MemberService;
import com.goodee.beedan.service.root.SecurityService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private final SecurityService securityService;
    private final SessionRegistry sessionRegistry;
    private final MemberService memberService;
    private final SessionLogRepository sessionLogRepository;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        SecurityPolicyDto policy = securityService.getSecPolDto();

        // 2. 세션 타임아웃 동적 설정
        if (policy.getIsSessionTimeoutEnabled()) {
            request.getSession().setMaxInactiveInterval(policy.getSessionTimeoutMinutes().intValue() * 60);
        }

        // 3. 중복 로그인 동적 제어
        String currentSessionId = request.getSession().getId();
        if (policy.getIsConcurrentLoginPrevented()) {
            List<SessionInformation> sessions = sessionRegistry.getAllSessions(authentication.getPrincipal(), false);
            for (SessionInformation session : sessions) {
                if (!session.getSessionId().equals(currentSessionId)) {
                    session.expireNow(); // 세션 만료처리
                }
            }
        }

        String username = authentication.getName();
        if (policy.getIsLoginSecurityEnabled()) {
            memberService.resetLoginStatus(username);
        }

        if (policy.getIsPasswordExpiryEnabled()) {
            request.getSession().setAttribute("LOGIN_TRIGGER", true);
        }

        // 세션 로그에 회원 ID 기록
        try {
            Object principal = authentication.getPrincipal();
            String sessionId = request.getSession().getId();
            if (principal instanceof MemberUserDetails) {
                Long memId = ((MemberUserDetails) principal).getMemberId();
                sessionLogRepository.findBySlSsId(sessionId).ifPresent(sl -> {
                    sl.setMemId(memId);
                    sessionLogRepository.save(sl);
                });
            }
        } catch (Exception e) { /* non-critical: session log */ }

        setDefaultTargetUrl("/");
        request.getSession().removeAttribute("loginTempStatus");

        super.onAuthenticationSuccess(request, response, authentication);
    }


}
