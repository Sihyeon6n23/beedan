package com.goodee.beedan.dto.member;

import com.goodee.beedan.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountStatusDto {
    private Long loginTryCount;
    private LocalDateTime accountLockDateTime;
    private String accountStatus;

    public AccountStatusDto(Member member) {
        this.loginTryCount = member.getMemLgnTr();
        this.accountLockDateTime = member.getMemLocDt();
        this.accountStatus = member.getMemStt();
    }
}
