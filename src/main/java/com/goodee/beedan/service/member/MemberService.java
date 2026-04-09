package com.goodee.beedan.service.member;

import com.goodee.beedan.common.constant.MemberAuthority;
import com.goodee.beedan.common.constant.MemberStatus;
import com.goodee.beedan.dto.admin.MemberListDto;
import com.goodee.beedan.dto.mail.PasswordResetMailRequest;
import com.goodee.beedan.dto.member.*;
import com.goodee.beedan.dto.root.security.SecurityPolicyDto;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.entity.Token;
import com.goodee.beedan.mapper.member.MemberMapper;
import com.goodee.beedan.repository.member.MemberRepository;
import com.goodee.beedan.repository.token.TokenRepository;
import com.goodee.beedan.service.mail.MailNotificationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.boot.model.naming.IllegalIdentifierException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MemberService {
    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;
    private final MailNotificationService mailNotificationService;

    @Value("${site.url}")
    private String siteUrl;

    public Member getMemberById(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("UsernameNotFoundException"));
    }

    public Member getMemberByUsername(String username) {
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

    public void insertMember(Member member) {
        try {
            memberRepository.save(member);
        } catch (DataIntegrityViolationException e) {
            // DB 제약 조건 위반 (중복 아이디, 중복 사업자번호 등)
            log.error("회원 저장 중 데이터 무결성 오류 발생: {}", e.getMessage());
            throw new IllegalStateException("이미 존재하는 회원 정보이거나 데이터가 올바르지 않습니다.", e);
        } catch (Exception e) {
            // 기타 예상치 못한 서버 에러
            log.error("회원 저장 중 알 수 없는 오류 발생", e);
            throw new RuntimeException("회원가입 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    public Boolean isDuplicatedLoginId(String username) {
        return memberRepository.existsByMemLgnId(username);
    }

    public void allowAccount(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new UsernameNotFoundException("계정을 찾을 수 없습니다."));
        member.approve();
    }

    public void inactiveAccount(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new UsernameNotFoundException("계정을 찾을 수 없습니다."));
        member.inactive();
    }

    public void resetPassword(String token, PasswordResetDto resetDto) {
        Member member = findMemberByToken(token);
        member.setMemLgnPw(passwordEncoder.encode(resetDto.getPassword()));
    }

    public void processPasswordReset(String loginId, String email) {
        // 1. 아이디와 이메일이 동시에 일치하는 회원 조회
        Member member = memberRepository.findByMemLgnIdAndMemEml(loginId, email)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));

        String tokenValue = UUID.randomUUID().toString();

        PasswordResetTokenDto tokenDto = PasswordResetTokenDto.builder()
                .tkVl(tokenValue)
                .memId(member.getMemId()) // 회원 PK 연결
                .tkTy("PASSWORD_RESET")   // 토큰 타입 구분
                .build();

        createToken(tokenDto);

        PasswordResetMailRequest mailRequest = new PasswordResetMailRequest(
                member.getMemEml(),
                member.getMemId(),
                tokenValue,
                siteUrl
        );

        mailNotificationService.sendNotification(mailRequest);
    }


    public Optional<String> findLoginId(String username, String email) {
        return memberRepository.findByMemNmAndMemEml(username, email)
                .map(member -> {
                    String rawId = member.getMemLgnId();
                    if (rawId.length() > 4) {
                        return rawId.substring(0, rawId.length() - 4) + "****";
                    }
                    return rawId + "****";
                });
    }

    private void createToken(PasswordResetTokenDto tokenDto) {
        LocalDateTime nowTime = LocalDateTime.now();

        Token token = Token.builder()
                .tkVl(tokenDto.getTkVl())
                .tkTy(tokenDto.getTkTy())
                .tkUseYn(false)
                .tkCreDt(nowTime)
                .tkExpDt(nowTime.plusMinutes(15))
                .member(memberRepository.findById(tokenDto.getMemId()).orElseThrow(() -> new EntityNotFoundException("사용자 ID를 찾을 수 없습니다.")))
                .build();

        tokenRepository.save(token);
    }

    public Member findMemberByToken(String token) {
        Token tokenEntity = tokenRepository.findByTkVl(token).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 토큰입니다."));

        if (tokenEntity.isExpired()) {
            throw new IllegalIdentifierException("토큰이 이미 사용되었거나, 기간이 만료된 토큰입니다.");
        }

        // tokenEntity.useToken(); 만료처리는 비밀번호 초기화가 완료된 이후?
        return tokenEntity.getMember();
    }

    private Boolean checkMemberAuthority(Long memId){
        Member member = memberRepository.findById(memId).orElseThrow(()-> new UsernameNotFoundException("일치하는 관리자가 없습니다."));
        return member.getMemAut().equals(MemberAuthority.ADMIN);
    }

}