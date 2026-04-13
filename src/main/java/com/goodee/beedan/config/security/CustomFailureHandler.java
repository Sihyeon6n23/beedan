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
        String errorMessage = "로그인에 실패했습니다.";

        try {
            Member member = memberService.getMemberByUsername(username);
            SecurityPolicyDto policy = securityService.getSecPolDto();
            HttpSession session = request.getSession();

            // DB값을 조회해서 Dto 생성 및 세션 임시저장
            AccountStatusDto accountStatusDto = new AccountStatusDto(member);
            if (session.getAttribute("loginTempStatus") == null) {
                session.setAttribute("loginTempStatus", accountStatusDto);
            }

            // 1. 비밀번호 불일치 (BadCredentialsException)
            if (exception instanceof BadCredentialsException) {
                errorMessage = "계정 정보가 일치하지 않습니다.";

                // DTO 변경점 반영: getIsLoginSecurityEnabled()
                if (Boolean.TRUE.equals(policy.getIsLoginSecurityEnabled())) {
                    // 실패 횟수 증가 및 DB 업데이트 반영 (Service 로직에 따라 구현됨)
                    accountStatusDto = memberService.increaseFailCount(member.getMemId(), policy, accountStatusDto);
                    session.setAttribute("loginTempStatus", accountStatusDto);

                    // 방금 실패로 인해 계정이 '잠금' 상태로 전환된 경우
                    if (MemberStatus.LOCK.name().equals(accountStatusDto.getAccountStatus())) {
                        errorMessage = "비밀번호 시도 횟수를 초과하여 계정이 잠겼습니다. "
                                + policy.getAccountLockDurationMinutes() + "분 후 다시 시도해주세요.";
                    } else {
                        // 계정은 아직 열려있지만 실패한 경우 남은 횟수 안내
                        long remainingCount = policy.getMaxLoginFailureCount() - accountStatusDto.getLoginTryCount();
                        errorMessage = "비밀번호가 일치하지 않습니다. (남은 시도 횟수: " + Math.max(0, remainingCount) + "회)";
                    }
                }
            }
            // 2. 계정 비활성화 및 대기 상태 (DisabledException)
            else if (exception instanceof DisabledException) {
                String status = accountStatusDto.getAccountStatus();
                if (MemberStatus.INACTIVE.name().equals(status)) {
                    errorMessage = "비활성화된 계정입니다. 관리자에게 문의해주시기 바랍니다.";
                } else if (MemberStatus.PENDING.name().equals(status)) {
                    errorMessage = "승인 절차를 진행중입니다. 영업일 1~2일 내로 처리됩니다.";
                } else {
                    errorMessage = "현재 사용할 수 없는 계정입니다.";
                }
            }
            // 3. 계정 잠김 (LockedException - 이미 잠긴 상태에서 로그인 시도 시)
            else if (exception instanceof LockedException) {
                LocalDateTime lockTime = accountStatusDto.getAccountLockDateTime();

                if (lockTime != null && policy.getAccountLockDurationMinutes() != null) {
                    // 잠금이 풀리는 시간 = 잠긴 시간 + 정책 상 잠금 유지 시간
                    LocalDateTime unlockTime = lockTime.plusMinutes(policy.getAccountLockDurationMinutes());
                    Duration duration = Duration.between(LocalDateTime.now(), unlockTime);

                    if (!duration.isNegative() && !duration.isZero()) {
                        errorMessage = "계정이 잠겨있습니다. " + (duration.toMinutes() + 1) + "분 이후에 다시 시도해주세요.";
                    } else {
                        // 시간이 이미 지났는데 LockedException이 넘어온 경우 (UserDetailsService의 해제 처리가 안 된 경우 대비 방어코드)
                        errorMessage = "계정 잠금 시간이 만료되었습니다. 다시 로그인하여 주시기 바랍니다.";
                    }
                } else {
                    errorMessage = "계정이 잠겼습니다. 관리자에게 문의해주세요.";
                }
            }

        } catch (UsernameNotFoundException e) {
            errorMessage = "계정 정보가 존재하지 않거나 일치하지 않습니다.";
        } finally {
            String encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
            setDefaultFailureUrl("/auth/signin?error=true&message=" + encodedMessage);
            super.onAuthenticationFailure(request, response, exception);
        }
    }
}