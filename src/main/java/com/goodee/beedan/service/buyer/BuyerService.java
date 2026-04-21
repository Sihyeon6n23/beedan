package com.goodee.beedan.service.buyer;

import com.goodee.beedan.dto.buyer.BuyerRequest;
import com.goodee.beedan.entity.Buyer;
import com.goodee.beedan.entity.BuyerGradePolicy;
import com.goodee.beedan.repository.buyer.BuyerGradePolicyRepository;
import com.goodee.beedan.repository.buyer.BuyerRepository;
import groovy.util.logging.Slf4j;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@lombok.extern.slf4j.Slf4j
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BuyerService {

    private final BuyerRepository buyerRepository;
    private final BuyerGradePolicyRepository buyerGradePolicyRepository;

    @Transactional
    public Buyer createOrFind(BuyerRequest request){
        return buyerRepository.findByMemBizNo(request.getMemBizNo())
                .orElseGet(() -> {

                    // 최소 등급 부여
                    String defaultGrade = buyerGradePolicyRepository
                            .findTopByBgpAcYnTrueOrderByBgpMinOrdCntAsc()
                                    .map(BuyerGradePolicy::getBgpGr)
                                            .orElseThrow(()-> new EntityNotFoundException(
                                                    "활성화된 등급 정책이 없습니다. 관리자에게 문의하세요."
                                            ));

                    log.info("신규 고객사 생성. 사업자번호: {}, 기본 등급: {}",
                            request.getMemBizNo(), defaultGrade);

                    return buyerRepository.save(
                            Buyer.builder()
                                    .memBizNo(request.getMemBizNo())
                                    .memBizTtl(request.getMemBizTtl())
                                    .bgpGr(defaultGrade)
                                    .build()
                    );
                });

    }
    public Buyer findById(Long byId) {
        return buyerRepository.findById(byId)
                .orElseThrow(()-> new EntityNotFoundException(
                        "고객사를 찾을 수 없습니다. id: "+byId
                ));
    }

    public Buyer findByBizNo(String memBizNo) {
        return buyerRepository.findByMemBizNo(memBizNo)
                .orElseThrow(()-> new EntityNotFoundException(
                        "고객사를 찾을 수 없습니다. 사업자번호: " + memBizNo
                ));
    }

    public Optional<Buyer> findByBizNoOptional(String memBizNo) {
        return buyerRepository.findByMemBizNo(memBizNo);
    }

    public List<Buyer> findAllByGrade(String grade){
        return buyerRepository.findAllByBgpGr(grade);
    }

    @Transactional
    public void updateAfterPayment(Long buyerId, BigDecimal paymentAmount){
        Buyer buyer = findById(buyerId);

        buyer.recordOrder(paymentAmount);
        log.info("실적 업데이트 완료. 누적 거래 횟수: {}, 누적 금액: {}",
                buyer.getByOrdCnt(), buyer.getByTtlAm());
    }

    @Transactional
    public void bulkResolveGrade() {

        List<BuyerGradePolicy> policies = buyerGradePolicyRepository
                .findAllByBgpAcYnTrueOrderByBgpMinOrdCntDesc();

        if (policies.isEmpty()){
            throw new EntityNotFoundException(
                    "활성화된 등급 정책이 없습니다.");
        }

        List<Buyer> allBuyers = buyerRepository.findAll();

        allBuyers.forEach(buyer -> {
            String newGrade = resolveGradeFromPolicies(buyer, policies);
            buyer.updateGrade(newGrade);
            log.info("등급 재산정. 사업자번호: {}, 기존 등급: {} -> 새 등급: {}",
                    buyer.getMemBizNo(), buyer.getBgpGr(), newGrade);
        });

        buyerRepository.saveAll(allBuyers);
        log.info("전체 등급 재산정 완료. 총 {}명", allBuyers.size());
    }

    private String resolveGradeFromPolicies(
            Buyer buyer,
            List<BuyerGradePolicy> policies
    ) {
        return policies.stream()
                .filter(BuyerGradePolicy::isValid)
                .filter(policy -> {
                    boolean countMet =
                            policy.getBgpMinOrdCnt() == null
                            || buyer.getByOrdCnt() >= policy.getBgpMinOrdCnt();
                    boolean amountMet =
                            policy.getBgpMinTtAm() == null
                            || buyer.getByTtlAm()
                                    .compareTo(policy.getBgpMinTtAm()) >= 0;
                    return countMet || amountMet;
                })
                .findFirst()
                .map(BuyerGradePolicy::getBgpGr)
                .orElse(policies.get(policies.size() -1).getBgpGr());
    }



}
