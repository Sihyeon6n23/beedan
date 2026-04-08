package com.goodee.beedan.config.security;

import com.goodee.beedan.entity.Member;
import com.goodee.beedan.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 사용자가 입력한 아이디를 사용해 멤버 리포지터리에서 회원을 조회하고,
        // 다시 이를 사용해 권한 리포지터리에서 해당 회원의 권한을 조회한 후
        // 우리가 확장 정의한 MemberUserDetails 객체로 만들어 반환
        Member member = memberRepository.findByMemLgnId(username).orElseThrow(() -> new UsernameNotFoundException("UsernameNotFound"));

        return new MemberUserDetails(member, null);
    }
}