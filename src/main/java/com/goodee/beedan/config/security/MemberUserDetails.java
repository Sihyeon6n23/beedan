package com.goodee.beedan.config.security;

import com.goodee.beedan.common.constant.MemberAuthority;
import com.goodee.beedan.common.constant.MemberStatus;
import com.goodee.beedan.entity.Member;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Data
public class MemberUserDetails implements UserDetails, OidcUser {
    private String username;
    private String password;
    private List<SimpleGrantedAuthority> authorities;
    private String displayName;
    private Long memberId;
    private String bizName;

    private String accountStatus;
    private Long loginTryCount;
    private LocalDateTime accountLockExpirationTime;

    public MemberUserDetails (Member member, OidcUser oidcUser) {
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

    // oidcUser 어쩔 수 없이 오버라이딩만 실제 미사용
    @Override
    public String getName() {
        return "";
    }

    @Override
    public Map<String, Object> getClaims() {
        return Map.of();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return null;
    }

    @Override
    public OidcIdToken getIdToken() {
        return null;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of();
    }
}