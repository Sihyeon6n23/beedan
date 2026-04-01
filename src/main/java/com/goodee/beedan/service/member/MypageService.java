package com.goodee.beedan.service.member;


import com.goodee.beedan.dto.member.*;
import com.goodee.beedan.dto.member.mypage.UpdateMemberRequest;
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

    public void updateMember(String username, UpdateMemberRequest updateDto) {
        log.info("=== [회원 정보 수정 시작] 요청자 ID: {} ===", username);

        // 1. 뷰(컨트롤러)에서 넘어온 DTO 확인
        log.info("👉 매핑 전 DTO 데이터: {}", updateDto);

        Member member = memberRepository.findByMemLgnId(username)
                .orElseThrow(() -> new UsernameNotFoundException("계정을 찾을 수 없습니다."));

        // (선택) 2. 매핑 전 엔티티의 원래 값 확인
        log.info("👉 매핑 전 Entity 원본 데이터 (이메일: {}, 폰: {})", member.getMemEml(), member.getMemMbPhn());

        // 3. MapStruct 매퍼 실행
        memberMapper.updateEntityFromDto(updateDto, member);

        // 4. 매퍼 실행 후 값이 제대로 덮어씌워졌는지 엔티티 확인
        log.info("✅ 매핑 후 변경된 Entity 데이터: {}", member);
        log.info("=== [회원 정보 수정 종료] ===");
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
