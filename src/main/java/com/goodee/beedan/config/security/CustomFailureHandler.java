package com.goodee.beedan.config.security;

import com.goodee.beedan.common.constant.MemberStatus;
import com.goodee.beedan.dto.member.AccountStatusDto;
import com.goodee.beedan.dto.root.security.SecurityPolicyDto;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.service.member.MemberService;
import com.goodee.beedan.service.root.SecurityService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
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
public class CustomFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    private final SecurityService securityService;
    private final MemberService memberService;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {
        String username = request.getParameter("username");

        String errorMessage="";
        try {
            Member member = memberService.getMemberByUsername(username);
            SecurityPolicyDto policy = securityService.getSecPolDto();
            // 계정 상태를 세션에 임시저장
            HttpSession session = request.getSession();

            // DB값을 조회해서 Dto 생성
            AccountStatusDto accountStatusDto = new AccountStatusDto(member);
            // 세션DTO를 조회, 세션에 없으면 세션에 새로 작성.
            if (session.getAttribute("loginTempStatus") == null) {
                session.setAttribute("loginTempStatus", accountStatusDto);
            }

            // 비밀번호 틀린 경우 메시지
            if (exception instanceof BadCredentialsException){
                errorMessage = "계정 정보가 일치하지 않습니다.";
                if (policy.getIsLoginFailureLimitEnabled()) {
                    System.out.println("비밀번호 시도 횟수 제한 정책이 켜져있습니다.");
                    System.out.println(accountStatusDto);
                    accountStatusDto = memberService.increaseFailCount(member.getMemId(), policy, accountStatusDto);
                    // 변경된 값을 다시 저장
                    session.setAttribute("loginTempStatus", accountStatusDto);
                }
            }
            else if(exception instanceof DisabledException) {

                String status = accountStatusDto.getAccountStatus();
                if (status.equals(MemberStatus.INACTIVE.toString())) {
                    errorMessage = "비활성화된 계정입니다. 전화로 문의해주시기 바랍니다.";
                } else if (status.equals(MemberStatus.PENDING.toString())) {
                    errorMessage = "승인 절차를 진행중입니다. 영업일 1~2일 내로 처리됩니다.";
                }
            }
            else if (exception instanceof LockedException) {
                // 잠긴 계정 예외 확인시 메시지
                Duration duration = Duration.between(LocalDateTime.now(),accountStatusDto.getAccountLockDateTime());
                errorMessage = "계정이 잠겼습니다."
                        + (duration.toMinutes() + 1)
                        + "분 이후에 다시 시도해주세요.";
            }

        } catch (UsernameNotFoundException e) {
            errorMessage="계정 정보가 일치하지 않습니다.";
        } finally {
            String encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);

            setDefaultFailureUrl("/auth/signin?error=true&message=" + encodedMessage);
            super.onAuthenticationFailure(request, response, exception);
        }
    }
}
