package com.goodee.beedan.service.buyer;

import com.goodee.beedan.common.policy.FeeCalculationType;
import com.goodee.beedan.dto.buyer.FeePolicyCreateRequest;
import com.goodee.beedan.dto.buyer.FeePolicyUpdateRequest;
import com.goodee.beedan.entity.FeePolicy;
import com.goodee.beedan.repository.buyer.FeePolicyRepository;
import groovy.util.logging.Slf4j;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeePolicyService {

    private final FeePolicyRepository feePolicyRepository;

    public FeePolicy create(FeePolicyCreateRequest request){

        if (feePolicyRepository
                .findByBgpGrAndFpFeeTyAndFpAcYnTrue(
                        request.getBgpGr(), request.getFpFeeTy()
                ).isPresent()) {
            throw new IllegalStateException(
                    "이미 활성화 된 정책이 존재합니다. 등급: " + request.getBgpGr()
            );
        }

        return feePolicyRepository.save(
                FeePolicy.builder()
                        .bgpGr(request.getBgpGr())
                        .fpFeeTy(request.getFpFeeTy())
                        .fpCalcTy(FeeCalculationType.valueOf(request.getFpCalcTy()))
                        .fpVal(request.getFpVal())
                        .fpEfFrDt(request.getFpEfFrDt())
                        .fpEfToDt(request.getFpEfToDt())
                        .fpDes(request.getFpDes())
                        .build()
        );
    }

    public FeePolicy findById(Long fpId) {
        return feePolicyRepository.findById(fpId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "비용 정책을 찾을 수 없습니다. id: " + fpId));
    }

    // 등급별 활성 정책 전체 조회
    // 견적 계산 시 사용
    public List<FeePolicy> findAllActiveByGrade(String bgpGr) {
        return feePolicyRepository.findAllByBgpGrAndFpAcYnTrue(bgpGr);
    }

    // 업데이트
    @Transactional
    public FeePolicy update(Long fpId, FeePolicyUpdateRequest request) {

        // 기존 정책 비활성화
        FeePolicy existing = findById(fpId);
        existing.deactivate();

        // 새 정책 생성
        return feePolicyRepository.save(
                FeePolicy.builder()
                        .bgpGr(existing.getBgpGr())
                        .fpFeeTy(existing.getFpFeeTy())
                        .fpCalcTy(FeeCalculationType.valueOf(request.getFpCalcTy()))
                        .fpVal(request.getFpVal())
                        .fpEfFrDt(request.getFpEfFrDt())
                        .fpEfToDt(request.getFpEfToDt())
                        .fpDes(request.getFpDes())
                        .build()
        );
    }
    @Transactional
    public void deactivate(Long fpId) {
        FeePolicy policy = findById(fpId);
        policy.deactivate();
    }

    public List<FeePolicy> findAllActive() {
        return feePolicyRepository.findAllByFpAcYnTrue();
    }
}
