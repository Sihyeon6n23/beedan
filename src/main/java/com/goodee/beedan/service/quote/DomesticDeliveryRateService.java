package com.goodee.beedan.service.quote;

import com.goodee.beedan.dto.quote.DomesticDeliveryRateRequest;
import com.goodee.beedan.entity.DomesticDeliveryRate;
import com.goodee.beedan.repository.quote.DomesticDeliveryRateRepository;
import groovy.util.logging.Slf4j;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@lombok.extern.slf4j.Slf4j
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DomesticDeliveryRateService {

    private final DomesticDeliveryRateRepository domesticDeliveryRateRepository;

    /**
     * 국내 배송비 등록
     * 같은 지역이 이미 있으면 예외 발생
     */
    @Transactional
    public DomesticDeliveryRate create(DomesticDeliveryRateRequest request) {

        if (domesticDeliveryRateRepository
                .findByDdrRgnAndDdrYnTrue(request.getDdrRgn())
                .isPresent()) {
            throw new IllegalStateException(
                    "이미 활성화된 배송비가 존재합니다. 지역: " + request.getDdrRgn()
                            + " 수정을 원하시면 update를 사용하세요.");
        }

        DomesticDeliveryRate rate = domesticDeliveryRateRepository.save(
                DomesticDeliveryRate.builder()
                        .region(request.getDdrRgn())
                        .amount(request.getDdrAm())
                        .extraAmount(request.getDdrEAm())
                        .description(request.getDdrDes())
                        .build()
        );
        log.info("국내 배송비 등록 완료. ID: {}, 지역: {}, 기본: {}원, 추가: {}원",
                rate.getDdrId(), rate.getDdrRgn(), rate.getDdrAm(), rate.getDdrEAm());
        return rate;
    }

    /**
     * 단건 조회
     */
    public DomesticDeliveryRate findById(Long ddrId) {
        return domesticDeliveryRateRepository.findById(ddrId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "국내 배송비를 찾을 수 없습니다. id: " + ddrId));
    }

    /**
     * 지역으로 활성 배송비 조회
     * 견적 계산 시 사용
     */
    public DomesticDeliveryRate findActiveByRegion(String ddrRgn) {
        return domesticDeliveryRateRepository.findByDdrRgnAndDdrYnTrue(ddrRgn)
                .orElseThrow(() -> new EntityNotFoundException(
                        "활성화된 배송비를 찾을 수 없습니다. 지역: " + ddrRgn));
    }

    /**
     * 활성 배송비 전체 조회
     */
    public List<DomesticDeliveryRate> findAllActive() {
        return domesticDeliveryRateRepository.findAllByDdrYnTrue();
    }

    /**
     * 국내 배송비 수정
     */
    @Transactional
    public DomesticDeliveryRate update(Long ddrId, DomesticDeliveryRateRequest request) {
        DomesticDeliveryRate rate = findById(ddrId);
        rate.update(
                request.getDdrAm(),
                request.getDdrEAm(),
                request.getDdrDes()
        );
        log.info("국내 배송비 수정 완료. ID: {}, 지역: {}, 기본: {}원, 추가: {}원",
                rate.getDdrId(), rate.getDdrRgn(), rate.getDdrAm(), rate.getDdrEAm());
        return rate;
    }

    /**
     * 국내 배송비 비활성화
     */
    @Transactional
    public void deactivate(Long ddrId) {
        DomesticDeliveryRate rate = findById(ddrId);
        rate.deactivate();
        log.info("국내 배송비 비활성화 완료. ID: {}, 지역: {}", rate.getDdrId(), rate.getDdrRgn());
    }
}