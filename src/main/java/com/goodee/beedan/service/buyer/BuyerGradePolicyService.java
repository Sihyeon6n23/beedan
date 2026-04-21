package com.goodee.beedan.service.buyer;

import com.goodee.beedan.dto.buyer.BuyerGradePolicyCreateRequest;
import com.goodee.beedan.dto.buyer.BuyerGradePolicyUpdateRequest;
import com.goodee.beedan.entity.BuyerGradePolicy;
import com.goodee.beedan.repository.buyer.BuyerGradePolicyRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BuyerGradePolicyService {

    private final BuyerGradePolicyRepository buyerGradePolicyRepository;

    @Transactional
    public BuyerGradePolicy create(BuyerGradePolicyCreateRequest request){

        if (buyerGradePolicyRepository
                .findByBgpGrAndBgpAcYnTrue(request.getBgpGr())
                .isPresent()){
            throw new IllegalStateException(
                    "이미 같은 명칭의 등급이 존재합니다. 수정을 원하시면 수정 기능을 사용하세요."
            );
        }

        return buyerGradePolicyRepository.save(
                BuyerGradePolicy.builder()
                        .bgpGr(request.getBgpGr())
                        .bgpMinOrdCnt(request.getBgpMinOrdCnt())
                        .bgpMinTtAm(request.getBgpMinTtAm())
                        .bgpEfFrDt(request.getBgpEfFrDt())
                        .bgpEfToDt(request.getBgpEfToDt())
                        .bgpDes(request.getBgpDes())
                        .build()
        );
    }

    @Transactional
    public BuyerGradePolicy update(
            Long bgpId,
            BuyerGradePolicyUpdateRequest request
    ){
        BuyerGradePolicy existing = findById(bgpId);
        existing.deactivate();

        return buyerGradePolicyRepository.save(
                BuyerGradePolicy.builder()
                        .bgpGr(existing.getBgpGr())         // 등급은 기존 것 유지
                        .bgpMinOrdCnt(request.getBgpMinOrdCnt())
                        .bgpMinTtAm(request.getBgpMinTtAm())
                        .bgpEfFrDt(request.getBgpEfFrDt())
                        .bgpEfToDt(request.getBgpEfToDt())
                        .bgpDes(request.getBgpDes())
                        .build()
        );


    }

    public BuyerGradePolicy findById(Long bgpId){
        return buyerGradePolicyRepository.findById(bgpId)
                .orElseThrow(()->new EntityNotFoundException(
                        "등급을 찾을 수 없습니다. id: "+ bgpId
                ));
    }

    public List<BuyerGradePolicy> findAllActive(){
        return buyerGradePolicyRepository.findAllByBgpAcYnTrue();
    }

    public List<BuyerGradePolicy> findAllActiveOrdered(){
        return buyerGradePolicyRepository
                .findAllByBgpAcYnTrueOrderByBgpMinOrdCntAsc();
    }

    public BuyerGradePolicy findActiveByGrade(String grade){
        return buyerGradePolicyRepository
                .findByBgpGrAndBgpAcYnTrue(grade)
                .orElseThrow(()-> new EntityNotFoundException(
                        "등급을 찾을 수 없습니다. 등급: "+ grade
                ));
    }

    @Transactional
    public void deactivate(Long bgpId) {
        BuyerGradePolicy policy = findById(bgpId);
        policy.deactivate();
    }





}
