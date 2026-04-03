package com.goodee.beedan.service.member;

import com.goodee.beedan.dto.member.sns.SnsIntegrateRequest;
import com.goodee.beedan.dto.member.sns.SnsIntegrateResponse;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.entity.SnsIntegrate;
import com.goodee.beedan.repository.member.sns.SnsIntegrateRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SnsIntegrateService {
    private final SnsIntegrateRepository snsIntegrateRepository;

    public SnsIntegrateResponse getSnsIntegrateResponse(Member member) {
        SnsIntegrate snsIntegrate = snsIntegrateRepository.findBySnsCanYnFalseAndMember(member)
                .orElseThrow(() -> new EntityNotFoundException("인증정보를 찾을 수 없습니다."));

        return SnsIntegrateResponse.builder()
                .snsTp(snsIntegrate.getSnsTp())
                .snsConDt(snsIntegrate.getSnsConDt())
                .connected(true)
                .build();
    }

    public Boolean isSnsIntegrate(Member member) {
        return snsIntegrateRepository.existsBySnsCanYnFalseAndMember(member);
    }

    public void setSnsIntegrateRequest(SnsIntegrateRequest request, Member member) {
//        SnsIntegrate snsIntegrate = SnsIntegrate.builder().build();
//        return snsIntegrateRepository.save(snsIntegrate);
    }
}
