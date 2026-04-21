package com.goodee.beedan.service.quote;

import com.goodee.beedan.dto.quote.QuoteDetailRequest;
import com.goodee.beedan.entity.QuoteDetail;
import com.goodee.beedan.repository.quote.QuoteDetailRepository;
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
public class QuoteDetailService {

    private final QuoteDetailRepository quoteDetailRepository;

    /**
     * 견적 품목 엔티티 직접 저장
     */
    @Transactional
    public QuoteDetail save(QuoteDetail entity) {
        return quoteDetailRepository.save(entity);
    }

    /**
     * 견적 품목 등록
     */
    @Transactional
    public QuoteDetail create(QuoteDetailRequest request) {
        QuoteDetail quoteDetail = quoteDetailRepository.save(
                QuoteDetail.builder()
                        .quoteInfoId(request.getQuInfoId())
                        .quoteId(request.getQuId())
                        .negoId(request.getNgId())
                        .stockId(request.getStId())
                        .stockQuantity(request.getQuDtQn())
                        .stockName(request.getStNm())
                        .factoryId(request.getFaId())
                        .factoryName(request.getFaNm())
                        .unitGroupId(request.getUnGId())
                        .unitGroupName(request.getUnGNm())
                        .unitGroupQuantity(request.getQuUQn())
                        .foreignPrice(request.getQuDtFgPr())
                        .build()
        );
        log.info("견적 품목 등록 완료. ID: {}, 상품명: {}, 수량: {}, 외화단가: {}",
                quoteDetail.getQuDtId(), quoteDetail.getStNm(),
                quoteDetail.getQuDtQn(), quoteDetail.getQuDtFgPr());
        return quoteDetail;
    }

    /**
     * 다건 등록
     */
    @Transactional
    public List<QuoteDetail> createAll(List<QuoteDetailRequest> requests) {
        List<QuoteDetail> details = requests.stream()
                .map(request -> QuoteDetail.builder()
                        .quoteInfoId(request.getQuInfoId())
                        .quoteId(request.getQuId())
                        .negoId(request.getNgId())
                        .stockId(request.getStId())
                        .stockQuantity(request.getQuDtQn())
                        .stockName(request.getStNm())
                        .factoryId(request.getFaId())
                        .factoryName(request.getFaNm())
                        .unitGroupId(request.getUnGId())
                        .unitGroupName(request.getUnGNm())
                        .unitGroupQuantity(request.getQuUQn())
                        .foreignPrice(request.getQuDtFgPr())
                        .build())
                .toList();

        List<QuoteDetail> saved = quoteDetailRepository.saveAll(details);
        log.info("견적 품목 다건 등록 완료. {}개", saved.size());
        return saved;
    }

    /**
     * 단건 조회
     */
    public QuoteDetail findById(Long quDtId) {
        return quoteDetailRepository.findById(quDtId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "견적 품목을 찾을 수 없습니다. id: " + quDtId));
    }

    /**
     * 견적 상세 아이디로 품목 전체 조회
     */
    public List<QuoteDetail> findAllByQuoteInfo(Long quInfoId) {
        return quoteDetailRepository.findAllByQuInfoId(quInfoId);
    }

    /**
     * 견적 아이디로 품목 전체 조회
     */
    public List<QuoteDetail> findAllByQuote(Long quId) {
        return quoteDetailRepository.findAllByQuId(quId);
    }

    /**
     * 공장별 품목 조회
     * QuoteShipFee 총 다스 수량 합산 기준
     */
    public List<QuoteDetail> findAllByQuoteInfoAndFactory(Long quInfoId, Long faId) {
        return quoteDetailRepository.findAllByQuInfoIdAndFaId(quInfoId, faId);
    }

    /**
     * 원화 환산 계산
     * 환율 적용 후 원화 단가 + 원화 합계 저장
     */
    @Transactional
    public QuoteDetail calculateKrwPrice(Long quDtId, BigDecimal exchangeRate) {
        QuoteDetail quoteDetail = findById(quDtId);
        quoteDetail.calculateKrwPrice(exchangeRate);
        log.info("원화 환산 완료. ID: {}, 원화단가: {}, 원화합계: {}",
                quDtId, quoteDetail.getQuDtKrPr(), quoteDetail.getQuDtPr());
        return quoteDetail;
    }

    /**
     * 공장별 총 다스 수량 합산
     * QuoteShipFee 배송비 구간 계산 기준
     */
    public Integer calculateTotalDozen(Long quInfoId, Long faId) {
        List<QuoteDetail> details = findAllByQuoteInfoAndFactory(quInfoId, faId);
        Integer totalDozen = details.stream()
                .mapToInt(d -> d.getTotalUnitCount() != null ? d.getTotalUnitCount() : 0)
                .sum();
        log.info("공장별 총 다스 수량. 견적상세ID: {}, 공장ID: {}, 총 다스: {}",
                quInfoId, faId, totalDozen);
        return totalDozen;
    }

    /**
     * 견적 품목 삭제
     */
    @Transactional
    public void delete(Long quDtId) {
        QuoteDetail quoteDetail = findById(quDtId);
        quoteDetailRepository.delete(quoteDetail);
        log.info("견적 품목 삭제 완료. ID: {}", quDtId);
    }
}