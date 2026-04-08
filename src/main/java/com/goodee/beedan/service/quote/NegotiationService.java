package com.goodee.beedan.service.quote;

import com.goodee.beedan.dto.quote.NegotiationRequest;
import com.goodee.beedan.entity.Negotiation;
import com.goodee.beedan.repository.quote.NegotiationRepository;
import groovy.util.logging.Slf4j;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@lombok.extern.slf4j.Slf4j
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NegotiationService {

    private final NegotiationRepository negotiationRepository;

    @Transactional
    public Negotiation create(NegotiationRequest request) {
        Negotiation negotiation = negotiationRepository.save(
                Negotiation.builder()
                        .name(request.getNgNm())
                        .memId(request.getMemId())
                        .build()
        );
        log.info("협상 생성 완료. ID: {}, 협상명: {}, 회원ID: {}",
                negotiation.getNgId(), negotiation.getNgNm(), negotiation.getMemId());
        return negotiation;
    }

    public Negotiation findById(Long ngId) {
        return negotiationRepository.findById(ngId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "협상을 찾을 수 없습니다. id: " + ngId));
    }

    public List<Negotiation> findAll() {
        return negotiationRepository.findAll();
    }

    public List<Negotiation> findAllByMember(Long memId) {
        return negotiationRepository.findAllByMemId(memId);
    }

    public Page<Negotiation> findAllByMember(Long memId, Pageable pageable) {
        return negotiationRepository.findAllByMemId(memId, pageable);
    }

    public List<Negotiation> findOngoingByMember(Long memId) {
        return negotiationRepository.findAllByMemIdAndNgEndDtIsNull(memId);
    }

    @Transactional
    public void close(Long ngId) {
        Negotiation negotiation = findById(ngId);
        negotiation.close();
        log.info("협상 종료 완료. ID: {}, 종료시간: {}", ngId, negotiation.getNgEndDt());
    }

    public Long getMemId(Long ngId){
        return negotiationRepository.findByNgId(ngId).getMemId();
    }

    /**
     * 협상의 마지막 견적 상태가 REJECTED/PAID/EXPIRED이면 협상 종료
     */
    @Transactional
    public void checkAndClose(Long ngId, com.goodee.beedan.repository.quote.QuoteBaseRepository quoteBaseRepository) {
        Negotiation negotiation = findById(ngId);
        if (!negotiation.isOngoing()) return; // 이미 종료

        List<com.goodee.beedan.entity.QuoteBase> quotes = quoteBaseRepository.findAllByNgId(ngId);
        if (quotes.isEmpty()) return;

        // 최신 견적 (quStt != null인 것 중 가장 최근)
        com.goodee.beedan.entity.QuoteBase latest = quotes.stream()
                .filter(q -> q.getQuStt() != null)
                .max(java.util.Comparator.comparing(q -> q.getQuCreDt() != null ? q.getQuCreDt() : java.time.LocalDateTime.MIN))
                .orElse(null);
        if (latest == null) return;

        var stt = latest.getQuStt();
        if (stt == com.goodee.beedan.common.constant.QuoteStatus.REJECTED
                || stt == com.goodee.beedan.common.constant.QuoteStatus.PAID
                || stt == com.goodee.beedan.common.constant.QuoteStatus.EXPIRED) {
            negotiation.close();
            log.info("협상 자동 종료. ngId: {}, 사유: 마지막 견적 상태 {}", ngId, stt);
        }
    }
}