package com.goodee.beedan.service.quote;

import com.goodee.beedan.dto.quote.QuoteInfoRequest;
import com.goodee.beedan.entity.QuoteInfo;
import com.goodee.beedan.repository.quote.QuoteInfoRepository;
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
public class QuoteInfoService {

    private final QuoteInfoRepository quoteInfoRepository;

    /**
     * 견적 상세 생성
     */
    @Transactional
    public QuoteInfo create(QuoteInfoRequest request) {
        QuoteInfo quoteInfo = quoteInfoRepository.save(
                QuoteInfo.builder()
                        .quoteId(request.getQuId())
                        .negoId(request.getNgId())
                        .currencyCode(request.getQuInfoCurCd())
                        .exchangeRate(request.getQuInfoExcRt())
                        .buyerGradePolicyId(request.getBgpId())
                        .feePolicyId(request.getFpId())
                        .build()
        );
        log.info("견적 상세 생성 완료. ID: {}, 견적ID: {}, 통화: {}, 환율: {}",
                quoteInfo.getQuInfoId(), quoteInfo.getQuId(),
                quoteInfo.getQuInfoCurCd(), quoteInfo.getQuInfoExcRt());
        return quoteInfo;
    }

    /**
     * 단건 조회
     */
    public QuoteInfo findById(Long quInfoId) {
        return quoteInfoRepository.findById(quInfoId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "견적 상세를 찾을 수 없습니다. id: " + quInfoId));
    }

    /**
     * 견적 아이디로 조회
     */
    public QuoteInfo findByQuoteId(Long quId) {
        return quoteInfoRepository.findByQuId(quId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "견적 상세를 찾을 수 없습니다. 견적ID: " + quId));
    }

    /**
     * 협상 아이디로 전체 조회
     */
    public List<QuoteInfo> findAllByNego(Long ngId) {
        return quoteInfoRepository.findAllByNgId(ngId);
    }

    /**
     * 서비스 수수료 계산 및 저장
     */
    @Transactional
    public QuoteInfo calculateServiceFee(Long quInfoId,
                                         BigDecimal itemTotal,
                                         BigDecimal feeRate,
                                         BigDecimal discountRate) {
        QuoteInfo quoteInfo = findById(quInfoId);
        quoteInfo.calculateServiceFee(itemTotal, feeRate, discountRate);
        log.info("서비스 수수료 계산 완료. ID: {}, 수수료: {}, 할인율: {}, 할인후: {}",
                quInfoId, quoteInfo.getQuInfoSrvFe(),
                quoteInfo.getQuInfoSrvFeR(), quoteInfo.getQuInfoSrvFeAm());
        return quoteInfo;
    }

    /**
     * 국내 배송비 저장
     */
    @Transactional
    public QuoteInfo setDomesticDelivery(Long quInfoId,
                                         BigDecimal amount,
                                         BigDecimal extraAmount) {
        QuoteInfo quoteInfo = findById(quInfoId);
        quoteInfo.setDomesticDelivery(amount, extraAmount);
        log.info("국내 배송비 저장 완료. ID: {}, 기본: {}, 추가: {}",
                quInfoId, quoteInfo.getQuInfoDdAm(), quoteInfo.getQuInfoDdExAm());
        return quoteInfo;
    }

    /**
     * 최종 합계 계산 및 저장
     */
    @Transactional
    public QuoteInfo calculateTotal(Long quInfoId,
                                    BigDecimal itemTotal,
                                    BigDecimal intShipTotal,
                                    BigDecimal taxTotal) {
        QuoteInfo quoteInfo = findById(quInfoId);
        quoteInfo.calculateTotal(itemTotal, intShipTotal, taxTotal);
        log.info("최종 합계 계산 완료. ID: {}, 국제배송비: {}, 전체배송비: {}, 관부가세: {}, 최종합계: {}",
                quInfoId, quoteInfo.getQuInfoIntShiFe(),
                quoteInfo.getQuInfoTtlShiFe(), quoteInfo.getQuInfoTax(),
                quoteInfo.getQuInfoTp());
        return quoteInfo;
    }
}