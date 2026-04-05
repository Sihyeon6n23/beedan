package com.goodee.beedan.service.member;

import com.goodee.beedan.dto.member.sns.SnsDisconnectRequest;
import com.goodee.beedan.dto.member.sns.SnsIntegrateRequest;
import com.goodee.beedan.dto.member.sns.SnsIntegrateResponse;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.entity.SnsIntegrate;
import com.goodee.beedan.repository.member.sns.SnsIntegrateRepository;
import com.goodee.beedan.service.auth.kakao.KakaoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SnsIntegrateService {
    private final SnsIntegrateRepository snsIntegrateRepository;
    private final KakaoService kakaoService;
    private final Map<String, SnsUnlinkService> unlinkServices;

    public SnsIntegrateResponse getSnsIntegrateResponse(Member member) {
        return snsIntegrateRepository.findBySnsCanYnFalseAndMember(member)
                .map(snsIntegrate -> SnsIntegrateResponse.builder()
                        .snsTp(snsIntegrate.getSnsTp().toString())
                        .snsConDt(snsIntegrate.getSnsConDt())
                        .connected(true)
                        .build())
                .orElseGet(() -> SnsIntegrateResponse
                        .builder()
                        .connected(false)
                        .build());
    }

    public Boolean isSnsIntegrate(Member member) {
        return snsIntegrateRepository.existsBySnsCanYnFalseAndMember(member);
    }

    public String getKakaoId(String code) {
        String accessToken = kakaoService.getAccessToken(code);
        return kakaoService.getKakaoId(accessToken).toString();
    }

    public void setSnsIntegrateRequest(SnsIntegrateRequest request, String code) {
        request.setSnsSeNo(getKakaoId(code));

        SnsIntegrate snsIntegrate = SnsIntegrate.builder()
                        .snsTp(request.getSnsTp())
                        .snsSeNo(request.getSnsSeNo())
                        .snsConDt(LocalDateTime.now())
                        .snsCanYn(false)
                        .member(request.getMember())
                        .build();

        snsIntegrateRepository.save(snsIntegrate);
    }

    public SnsDisconnectRequest getSnsDisconnectRequest(Member member) {
        return snsIntegrateRepository.findBySnsCanYnFalseAndMember(member)
                .map(sns -> SnsDisconnectRequest.builder()
                        .snsTp(sns.getSnsTp())
                        .snsSeNo(sns.getSnsSeNo())
                        .member(member)
                        .build())
                .orElseThrow(() -> new EntityNotFoundException("인증정보를 찾을 수 없습니다."));
    }

    public Mono<String> disconnect(SnsDisconnectRequest snsDisconnectRequest) {
        SnsUnlinkService unlinkService = Optional.ofNullable(unlinkServices.get(snsDisconnectRequest.getSnsTp()))
                        .orElseThrow(() -> new EntityNotFoundException("지원하지 않는 SNS 타입입니다."));

        return unlinkService.unlink(snsDisconnectRequest)
                .flatMap(resultSeNo -> {
                    if (snsDisconnectRequest.getSnsSeNo().equals(resultSeNo)) {
                        SnsIntegrate sns = snsIntegrateRepository.findBySnsCanYnFalseAndMember(snsDisconnectRequest.getMember())
                                .orElseThrow(() -> new EntityNotFoundException("인증 정보를 찾을 수 없습니다."));

                        sns.setSnsCanYn(true);
                        return Mono.just("SNS 연동을 해제했습니다.");
                    } else {
                        return Mono.error(new RuntimeException("SNS 연동을 실패했습니다."));
                    }
                });
    }
}
