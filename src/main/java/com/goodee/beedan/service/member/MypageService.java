package com.goodee.beedan.service.member;


import com.goodee.beedan.dto.member.*;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.mapper.member.MemberMapper;
import com.goodee.beedan.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MypageService {
    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    public void editMember(Long memberId, EditMemberDto editMemberDto) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new UsernameNotFoundException("계정을 찾을 수 없습니다."));
        memberMapper.updateEntityFromDto(editMemberDto, member);
    }

    public void changPassword(Long memberId, PasswordChangeDto changeDto) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new UsernameNotFoundException("계정을 찾을 수 없습니다."));
        String memberCurrentPassword = member.getMemLgnPw();
        String dtoCurrentPassword = changeDto.getCurrentPassword();
        if (!passwordEncoder.matches(dtoCurrentPassword, memberCurrentPassword)) {
            return;
        }

        member.setMemLgnPw(passwordEncoder.encode(changeDto.getPassword()));
    }

    public void withdraw(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new UsernameNotFoundException("계정을 찾을 수 없습니다."));
        member.withdraw();
    }
}
