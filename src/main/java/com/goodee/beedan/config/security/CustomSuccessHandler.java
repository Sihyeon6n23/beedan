package com.goodee.beedan.config.security;

import com.goodee.beedan.dto.root.security.SecurityPolicyDto;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.service.member.MemberService;
import com.goodee.beedan.service.root.SecurityService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor

public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final SecurityService securityService;
    private final SessionRegistry sessionRegistry;
    private final MemberService memberService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        SecurityPolicyDto policy = securityService.getSecPolDto();

        // 중복로그인 정책
        Object principal = authentication.getPrincipal();
        if (policy.getIsConcurrentLoginPrevented()) {

            List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);

            if (!sessions.isEmpty()) {
                for (SessionInformation session : sessions) {
                    session.expireNow();
                }
                System.out.println("[보안정책적용] 기존 세션을 만료처리하였습니다.");
            }
        }

        String username = authentication.getName();
        if (policy.getIsLoginFailureLimitEnabled()) {
            memberService.resetLoginStatus(username);
        }

        // 비밀번호 변경 정책 ON -> 비밀번호 변경시기 확인.?.....수정일 사용 불가.. 추가컬럼필요
        if (policy.getIsPasswordExpiryEnabled()) {
            HttpSession session = request.getSession();
            // session에 올리고 메인화면 갔을때 해당 세션객체가 있으면
            // 비밀번호 변경유도 창 띄워주기.
            session.setAttribute("passwordExpiration", true);
        }


        setDefaultTargetUrl("/mypage");

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
