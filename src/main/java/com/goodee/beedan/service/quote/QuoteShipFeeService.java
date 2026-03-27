package com.goodee.beedan.service.quote;

import com.goodee.beedan.dto.quote.QuoteShipFeeRequest;
import com.goodee.beedan.entity.QuoteShipFee;
import com.goodee.beedan.repository.quote.QuoteShipFeeRepository;
import groovy.util.logging.Slf4j;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@lombok.extern.slf4j.Slf4j
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuoteShipFeeService {

    private final QuoteShipFeeRepository quoteShipFeeRepository;

    /**
     * 견적 배송비 생성
     */
    @Transactional
    public QuoteShipFee create(QuoteShipFeeRequest request) {
        QuoteShipFee quoteShipFee = quoteShipFeeRepository.save(
                QuoteShipFee.builder()
                        .quoteInfoId(request.getQuInfoId())
                        .quoteId(request.getQuId())
                        .negoId(request.getNgId())
                        .factoryId(request.getFaId())
                        .factoryName(request.getQsfFaNm())
                        .factoryCountryCode(request.getQsfFaCCd())
                        .transportType(request.getQsfTrspTy())
                        .totalDozen(request.getQsfTtDz())
                        .build()
        );
        log.info("견적 배송비 생성 완료. ID: {}, 공장: {}, 국가: {}, 운송수단: {}, 총다스: {}",
                quoteShipFee.getQsfId(), quoteShipFee.getQsfFaNm(),
                quoteShipFee.getQsfFaCCd(), quoteShipFee.getQsfTrspTy(),
                quoteShipFee.getQsfUnQn());
        return quoteShipFee;
    }

    /**
     * 단건 조회
     */
    public QuoteShipFee findById(Long qsfId) {
        return quoteShipFeeRepository.findById(qsfId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "견적 배송비를 찾을 수 없습니다. id: " + qsfId));
    }

    /**
     * 견적 상세별 배송비 전체 조회
     */
    public List<QuoteShipFee> findAllByQuoteInfo(Long quInfoId) {
        return quoteShipFeeRepository.findAllByQuInfoId(quInfoId);
    }

    /**
     * 공장별 배송비 조회
     */
    public QuoteShipFee findByQuoteInfoAndFactory(Long quInfoId, Long faId) {
        return quoteShipFeeRepository.findByQuInfoIdAndFaId(quInfoId, faId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "견적 배송비를 찾을 수 없습니다. 견적상세ID: " + quInfoId
                                + ", 공장ID: " + faId));
    }

    /**
     * 해외 운임 자동 계산 결과 저장
     */
    @Transactional
    public QuoteShipFee setShippingFee(Long qsfId, BigDecimal amount) {
        QuoteShipFee quoteShipFee = findById(qsfId);
        quoteShipFee.setShippingFee(amount);
        log.info("해외 운임 저장 완료. ID: {}, 운임: {}원", qsfId, amount);
        return quoteShipFee;
    }

    /**
     * 해외 운임 수동 override
     * 특수 화물 등 관리자가 직접 수정
     */
    @Transactional
    public QuoteShipFee overrideShippingFee(Long qsfId,
                                            BigDecimal amount,
                                            String reason) {
        QuoteShipFee quoteShipFee = findById(qsfId);
        quoteShipFee.overrideShippingFee(amount, reason);
        log.info("해외 운임 수동 수정 완료. ID: {}, 운임: {}원, 사유: {}",
                qsfId, amount, reason);
        return quoteShipFee;
    }

    /**
     * 항만/통관 비용 저장
     */
    @Transactional
    public QuoteShipFee setPortCustomsFee(Long qsfId,
                                          BigDecimal portFee,
                                          BigDecimal customsFee,
                                          BigDecimal hsCodeFee) {
        QuoteShipFee quoteShipFee = findById(qsfId);
        quoteShipFee.setPortCustomsFee(portFee, customsFee, hsCodeFee);
        log.info("항만/통관 비용 저장 완료. ID: {}, 항만: {}원, 통관: {}원, HS: {}원",
                qsfId, portFee, customsFee, hsCodeFee);
        return quoteShipFee;
    }

    /**
     * 보험 가입 및 보험료 저장
     */
    @Transactional
    public QuoteShipFee setInsurance(Long qsfId, BigDecimal insuranceFee) {
        QuoteShipFee quoteShipFee = findById(qsfId);
        quoteShipFee.setInsurance(insuranceFee);
        log.info("보험료 저장 완료. ID: {}, 보험료: {}원", qsfId, insuranceFee);
        return quoteShipFee;
    }

    /**
     * CIF 계산
     * CIF = 상품가 + 해외운임 + 보험료
     */
    @Transactional
    public QuoteShipFee calculateCif(Long qsfId, BigDecimal itemTotal) {
        QuoteShipFee quoteShipFee = findById(qsfId);
        quoteShipFee.calculateCif(itemTotal);
        log.info("CIF 계산 완료. ID: {}, CIF: {}원", qsfId, quoteShipFee.getQsfCifAm());
        return quoteShipFee;
    }

    /**
     * 관세 / 부가세 계산
     * 관세 = CIF × 관세율
     * 부가세 = (CIF + 관세) × 10%
     */
    @Transactional
    public QuoteShipFee calculateDutyAndVat(Long qsfId, BigDecimal dutyRate) {
        QuoteShipFee quoteShipFee = findById(qsfId);
        quoteShipFee.calculateDutyAndVat(dutyRate);
        log.info("관세/부가세 계산 완료. ID: {}, 관세율: {}, 관세: {}원, 부가세: {}원",
                qsfId, dutyRate, quoteShipFee.getQsfDty(), quoteShipFee.getQsfVat());
        return quoteShipFee;
    }

    /**
     * 배송비 최종 합계 계산
     */
    @Transactional
    public QuoteShipFee calculateTotal(Long qsfId) {
        QuoteShipFee quoteShipFee = findById(qsfId);
        quoteShipFee.calculateTotal();
        log.info("배송비 최종 합계 계산 완료. ID: {}, 할인율: {}, 할인금액: {}원, 최종합계: {}원",
                qsfId, quoteShipFee.getQsfDscR(),
                quoteShipFee.getQsfDscAm(), quoteShipFee.getQsfTtl());
        return quoteShipFee;
    }

    /**
     * 견적 상세별 배송비 합계
     * QuoteInfo 의 국제 배송비 합계 계산 시 사용
     */
    public BigDecimal calculateTotalShipFee(Long quInfoId) {
        List<QuoteShipFee> fees = findAllByQuoteInfo(quInfoId);
        BigDecimal total = fees.stream()
                .map(f -> f.getQsfTtl() != null ? f.getQsfTtl() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        log.info("견적 상세 배송비 합계. 견적상세ID: {}, 합계: {}원", quInfoId, total);
        return total;
    }
}