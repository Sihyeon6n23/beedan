package com.goodee.beedan.config.security;

import com.goodee.beedan.common.constant.MemberStatus;
import com.goodee.beedan.dto.member.AccountStatusDto;
import com.goodee.beedan.dto.member.auth.SignInErrorMessageDto;
import com.goodee.beedan.dto.root.security.SecurityPolicyDto;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.service.member.MemberService;
import com.goodee.beedan.service.root.SecurityService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final MemberService memberService;
    private final SecurityService securityService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String username = request.getParameter("username");
        SecurityPolicyDto policy = securityService.getSecPolDto();
        HttpSession session = request.getSession();
        String errorMessage;

        try {
            // 1. 세션 기반 최신 상태 로드 (Lazy Loading 적용)
            AccountStatusDto status = getOrSyncStatus(session, username);

            // 2. 예외 타입별 전략적 메시지 결정
            errorMessage = determineErrorMessage(exception, status, policy, session);

        } catch (UsernameNotFoundException e) {
            errorMessage = "계정 정보가 존재하지 않거나 일치하지 않습니다.";
        } catch (Exception e) {
            log.error("인증 실패 처리 중 오류 발생: ", e);
            errorMessage = "로그인 처리 중 오류가 발생했습니다.";
        }

        request.getSession().setAttribute("errorMessage", new SignInErrorMessageDto("인증실패", errorMessage));
        setDefaultFailureUrl("/auth/signin");
        super.onAuthenticationFailure(request, response, exception);
    }

    /**
     * 예외별 메시지 분기
     */
    private String determineErrorMessage(AuthenticationException ex, AccountStatusDto status,
                                         SecurityPolicyDto policy, HttpSession session) {
        if (ex instanceof LockedException) {
            return formatLockedMessage(status, policy);
        }

        // 2. 비활성화 (DisabledException)
        if (ex instanceof DisabledException) {
            return "보안 정책에 의해 사용이 제한된 계정입니다. 관리자에게 문의하세요.";
        }

        // 3. 존재하지 않는 아이디 or 비밀번호 불일치 (통합 처리)
        if (ex instanceof BadCredentialsException) {
            // 내부적으로 실패 카운트는 올리되, 사용자에게는 공용 메시지 노출
            handleBadCredentials(status, policy, session);
            return "계정 정보가 존재하지 않거나 일치하지 않습니다.";
        }

        return "인증에 실패하였습니다. 다시 시도해주세요.";
    }

    private void handleBadCredentials(AccountStatusDto status, SecurityPolicyDto policy, HttpSession session) {
        AccountStatusDto updatedStatus = memberService.increaseFailCount(status.getMemId(), policy, status);
        session.setAttribute("loginTempStatus", updatedStatus);
    }

    private String formatLockedMessage(AccountStatusDto status, SecurityPolicyDto policy) {
        long waitMinutes = status.getMinutesUntilUnlock();

        if (waitMinutes > 0) {
            return String.format("해당 계정은 보안 정책에 의해 잠긴 상태입니다. %d분 후 다시 시도해주세요.", waitMinutes);
        }

        return "계정 정보가 존재하지 않거나 일치하지 않습니다.";
    }

    private AccountStatusDto getOrSyncStatus(HttpSession session, String username) {
        AccountStatusDto sessionStatus = (AccountStatusDto) session.getAttribute("loginTempStatus");

        if (sessionStatus == null || !username.equals(sessionStatus.getUsername())) {
            Member member = memberService.getMemberByUsername(username); // 여기서 UsernameNotFoundException 발생 가능
            sessionStatus = new AccountStatusDto(member);
            session.setAttribute("loginTempStatus", sessionStatus);
        }

        return sessionStatus;
    }
}