package com.goodee.beedan.service.exchangeRate;

import com.goodee.beedan.client.exchangeRate.ExchangeRateClient;
import com.goodee.beedan.dto.exchangeRate.ExchangeRateRequest;
import com.goodee.beedan.entity.ExchangeRate;
import com.goodee.beedan.repository.exchangeRate.ExchangeRateRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;

    /**
     * 환율 등록
     * 환율은 수정 없이 새로 추가하는 방식
     */
    @Transactional
    public ExchangeRate create(ExchangeRateRequest request) {
        ExchangeRate exchangeRate = exchangeRateRepository.save(
                ExchangeRate.builder()
                        .currency(request.getErCr())
                        .rate(request.getErRa())
                        .fetchedAt(request.getErFDt())
                        .build()
        );
        log.info("환율 등록 완료. ID: {}, 통화: {}, 환율: {}, 기준시간: {}",
                exchangeRate.getErId(), exchangeRate.getErCr(),
                exchangeRate.getErRa(), exchangeRate.getErFDt());
        return exchangeRate;
    }

    /**
     * 단건 조회
     */
    public ExchangeRate findById(Long erId) {
        return exchangeRateRepository.findById(erId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "환율을 찾을 수 없습니다. id: " + erId));
    }

    /**
     * 통화별 최신 환율 조회
     * 견적 계산 시 사용
     */
    public ExchangeRate findLatestByCurrency(String erCr) {
        return exchangeRateRepository.findTopByErCrOrderByErFDtDesc(erCr)
                .orElseThrow(() -> new EntityNotFoundException(
                        "환율 정보를 찾을 수 없습니다. 통화: " + erCr));
    }

    /**
     * 통화별 환율 이력 전체 조회
     */
    public List<ExchangeRate> findAllByCurrency(String erCr) {
        return exchangeRateRepository.findAllByErCrOrderByErFDtDesc(erCr);
    }
}