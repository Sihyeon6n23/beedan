package com.goodee.beedan.config.security;

import com.goodee.beedan.common.constant.MemberAuthority;
import com.goodee.beedan.common.constant.MemberStatus;
import com.goodee.beedan.entity.Member;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Data
public class MemberUserDetails implements UserDetails {
    private String username;
    private String password;
    private List<SimpleGrantedAuthority> authorities;
    private String displayName;
    private Long memberId;
    private String bizName;

    private String accountStatus;
    private Long loginTryCount;
    private LocalDateTime accountLockExpirationTime;

    public MemberUserDetails (Member member) {
        this.username = member.getMemLgnId();
        this.displayName = member.getMemNm(); // 그 대신 displayName으로 회원의 이름을 저장
        this.bizName = member.getMemBizTtl(); // 사업자명 추가
        this.password = member.getMemLgnPw();
        this.memberId = member.getMemId(); // 나중에 게시글을 작성하거나 수정할 때는 멤버 아이디가 필요하므로 memberId 필드를 추가
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + member.getMemAut().name()));


        this.accountStatus = member.getMemStt();
        this.loginTryCount = member.getMemLgnTr();
        this.accountLockExpirationTime = member.getMemLocDt();
    }


    @Override
    public boolean isAccountNonLocked() {
        if (accountStatus.equals(MemberStatus.ACTIVE.toString()) || accountLockExpirationTime == null) {
            System.out.println(accountLockExpirationTime);
            return true;
        } else {
            System.out.println(accountLockExpirationTime.isBefore(LocalDateTime.now()));
            return accountLockExpirationTime.isBefore(LocalDateTime.now());
        }
    }
    // ACTIVE만 TRUE
    @Override
    public boolean isEnabled() {
        System.out.println(this.accountStatus.equals(MemberStatus.ACTIVE.toString()));
        if (this.accountStatus.equals(MemberStatus.LOCK.toString())) {
            return true;
        }
        return this.accountStatus.equals(MemberStatus.ACTIVE.toString());
    }
}