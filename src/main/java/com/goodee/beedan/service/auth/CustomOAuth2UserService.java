package com.goodee.beedan.service.auth;

import com.goodee.beedan.common.constant.MemberStatus;
import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.repository.member.MemberRepository;
import com.goodee.beedan.repository.member.sns.SnsIntegrateRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CustomOAuth2UserService extends OidcUserService {

    private final MemberRepository memberRepository;
    private final SnsIntegrateRepository snsIntegrateRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 카카오 인증 정보 로드
        OidcUser oidcUser = super.loadUser(userRequest);

        // 2. 카카오 식별자 추출
        String kakaoId = oidcUser.getAttribute("sub").toString();

        log.info("여기까진 오나");

        // 3. ⭐️ 연동 테이블 조회 + 연관된 Member(user 계정)까지 한 번에 가져오기
        Member member = snsIntegrateRepository.findBySnsCanYnFalseAndSnsSeNo(kakaoId)
                .orElseThrow(() -> new OAuth2AuthenticationException("NOT_LINKED_ACCOUNT"))
                .getMember(); // SnsIntegrate 엔티티에서 Member를 꺼냄

        if (MemberStatus.valueOf(member.getMemStt()) == MemberStatus.LOCK) {
            if (!member.getMemLocDt().isAfter(LocalDateTime.now())) {
                throw new OAuth2AuthenticationException("ACCOUNT_LOCKED");
            }
        } else if (MemberStatus.valueOf(member.getMemStt()) == MemberStatus.INACTIVE){
            throw new OAuth2AuthenticationException("비활성화된 계정입니다.");
        }

        return new MemberUserDetails(member, oidcUser);
    }
}