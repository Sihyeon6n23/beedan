package com.goodee.beedan.service.admin;

import com.goodee.beedan.dto.admin.MemberEditRequest;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.mapper.member.MemberEditRequestToMemberMapper;
import com.goodee.beedan.repository.member.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminMemberService {
    private final MemberRepository memberRepository;
    private final MemberEditRequestToMemberMapper memberMapper;

    @Transactional
    public void updateMember(MemberEditRequest request) {
        // 1. 기존 데이터 조회
        Member member = memberRepository.findById(request.getId())
                .orElseThrow(() -> new EntityNotFoundException("회원 없음"));

        memberMapper.updateMemberFromDto(request, member);
    }
}
