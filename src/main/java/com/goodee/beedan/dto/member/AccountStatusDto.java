package com.goodee.beedan.dto.member;

import com.goodee.beedan.common.constant.MemberStatus;
import com.goodee.beedan.dto.root.security.SecurityPolicyDto;
import com.goodee.beedan.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class AccountStatusDto {
    private Long memId;
    private String username;
    private Long loginTryCount;
    private LocalDateTime accountLockDateTime;
    private String accountStatus;

    public AccountStatusDto(Member member) {
        this.memId = member.getMemId();
        this.username = member.getMemLgnId();
        this.loginTryCount = member.getMemLgnTr();
        this.accountLockDateTime = member.getMemLocDt();
        this.accountStatus = member.getMemStt();
    }

    public boolean isLocked() {
        return MemberStatus.LOCK.name().equals(this.accountStatus);
    }

    public long getRemainingCount(SecurityPolicyDto policy) {
        return Math.max(0, policy.getMaxLoginFailureCount() - this.loginTryCount);
    }

    public long getMinutesUntilUnlock() {
        if (this.accountLockDateTime == null) return 0;
        return Duration.between(LocalDateTime.now(), this.accountLockDateTime).toMinutes() + 1;
    }
}
