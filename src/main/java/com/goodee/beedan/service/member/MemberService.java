package com.goodee.beedan.service.member;

import com.goodee.beedan.common.constant.MemberStatus;
import com.goodee.beedan.dto.member.AccountStatusDto;
import com.goodee.beedan.dto.root.security.SecurityPolicyDto;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;

    public Member getLoginId(String username) {
        return memberRepository.findByMemLgnId(username)
                .orElseThrow(() -> new UsernameNotFoundException("UsernameNotFoundException"));
    }

    public AccountStatusDto increaseFailCount(
            Long memberId,
            SecurityPolicyDto policy,
            AccountStatusDto accountStatus)
    {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new UsernameNotFoundException("UsernameNotFoundException"));
        Long loginTryCount = accountStatus.getLoginTryCount();

        // 정책: 최대 시도 횟수
        Long maxLoginFailureCount = policy.getMaxLoginFailureCount();
        Long accountLockDurationMinutes = policy.getAccountLockDurationMinutes();

        // 시도 횟수 도달시 잠금 로직
        // 비활성화 계정은 왜 잠기는것..
        if (loginTryCount < maxLoginFailureCount) {
            member.setMemLgnTr(++loginTryCount);
            accountStatus.setLoginTryCount(loginTryCount);
        }

        if (loginTryCount.equals(maxLoginFailureCount)) {
            LocalDateTime now = LocalDateTime.now();
            member.setMemLocDt(now.plusMinutes(accountLockDurationMinutes));
            member.setMemStt(MemberStatus.LOCK.toString());
            accountStatus.setAccountStatus(MemberStatus.LOCK.toString());
        }

        return accountStatus;
    }

    public void resetLoginStatus(String username) {
        Member member = memberRepository.findByMemLgnId(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username Not FOUND"));
        member.setMemLgnTr(0L);
        member.setMemStt(MemberStatus.ACTIVE.toString());
    }
}
