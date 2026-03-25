package com.goodee.beedan.config.security;

import com.goodee.beedan.common.constant.MemberAuthority;
import com.goodee.beedan.entity.Member;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
public class MemberUserDetails implements UserDetails {
    private String username;
    private String password;
    private List<SimpleGrantedAuthority> authorities;
    private String displayName;
    private Long memberId;

    public MemberUserDetails (Member member) {
        this.username = member.getMemLgnId();
        this.displayName = member.getMemNm(); // 그 대신 displayName으로 회원의 이름을 저장
        this.password = member.getMemLgnPw();
        this.memberId = member.getMemId(); // 나중에 게시글을 작성하거나 수정할 때는 멤버 아이디가 필요하므로 memberId 필드를 추가
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + member.getMemAut().name()));
    }



}