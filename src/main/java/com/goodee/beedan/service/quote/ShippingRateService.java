package com.goodee.beedan.service.quote;

import com.goodee.beedan.common.constant.TransportType;
import com.goodee.beedan.dto.quote.ShippingRateRequest;
import com.goodee.beedan.dto.quote.ShippingRateUpdateRequest;
import com.goodee.beedan.entity.ShippingRate;
import com.goodee.beedan.repository.quote.ShippingRateRepository;
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
public class ShippingRateService {

    private final ShippingRateRepository shippingRateRepository;

    /**
     * 해외 운임표 등록
     * 같은 국가코드 + 운송수단 조합이 이미 있으면 예외 발생
     */
    @Transactional
    public ShippingRate create(ShippingRateRequest request) {

        if (shippingRateRepository
                .findBySrCCdAndSrTrspTyAndSrYnTrue(
                        request.getSrCCd(), request.getSrTrspTy())
                .isPresent()) {
            throw new IllegalStateException(
                    "이미 활성화된 운임표가 존재합니다. 국가코드: " + request.getSrCCd()
                            + ", 운송수단: " + request.getSrTrspTy()
                            + " 수정을 원하시면 update를 사용하세요.");
        }

        ShippingRate shippingRate = shippingRateRepository.save(
                ShippingRate.builder()
                        .countryCode(request.getSrCCd())
                        .transportType(request.getSrTrspTy())
                        .smallQuantity(request.getSrSmQn())
                        .smallAmount(request.getSrSmAm())
                        .mediumQuantity(request.getSrMdQn())
                        .mediumAmount(request.getSrMdAm())
                        .largeQuantity(request.getSrLgQn())
                        .largeAmount(request.getSrLgAm())
                        .description(request.getSrDes())
                        .build()
        );
        log.info("해외 운임표 등록 완료. ID: {}, 국가: {}, 운송수단: {}",
                shippingRate.getSrId(), shippingRate.getSrCCd(), shippingRate.getSrTrspTy());
        return shippingRate;
    }

    /**
     * 단건 조회
     */
    public ShippingRate findById(Long srId) {
        return shippingRateRepository.findById(srId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "해외 운임표를 찾을 수 없습니다. id: " + srId));
    }

    /**
     * 국가코드 + 운송수단으로 활성 운임표 조회
     * 견적 계산 시 핵심 메서드
     */
    public ShippingRate findByCCdAndTransportType(String srCCd, TransportType srTrspTy) {
        return shippingRateRepository
                .findBySrCCdAndSrTrspTyAndSrYnTrue(srCCd, srTrspTy)
                .orElseThrow(() -> new EntityNotFoundException(
                        "해외 운임표를 찾을 수 없습니다. 국가코드: " + srCCd
                                + ", 운송수단: " + srTrspTy));
    }

    /**
     * 국가코드별 활성 운임표 전체 조회
     */
    public List<ShippingRate> findAllByCountry(String srCCd) {
        return shippingRateRepository.findAllBySrCCdAndSrYnTrue(srCCd);
    }

    /**
     * 활성 운임표 전체 조회
     */
    public List<ShippingRate> findAllActive() {
        return shippingRateRepository.findAllBySrYnTrue();
    }

    /**
     * 운임표 수정
     * 운임비만 수정 가능 (국가코드, 운송수단 변경 불가)
     */
    @Transactional
    public ShippingRate update(Long srId, ShippingRateUpdateRequest request) {
        ShippingRate shippingRate = findById(srId);
        shippingRate.update(
                request.getSrSmAm(),
                request.getSrMdAm(),
                request.getSrLgAm(),
                request.getSrDes()
        );
        log.info("해외 운임표 수정 완료. ID: {}, 국가: {}, 운송수단: {}",
                shippingRate.getSrId(), shippingRate.getSrCCd(), shippingRate.getSrTrspTy());
        return shippingRate;
    }

    /**
     * 운임표 비활성화
     */
    @Transactional
    public void deactivate(Long srId) {
        ShippingRate shippingRate = findById(srId);
        shippingRate.deactivate();
        log.info("해외 운임표 비활성화 완료. ID: {}, 국가: {}, 운송수단: {}",
                shippingRate.getSrId(), shippingRate.getSrCCd(), shippingRate.getSrTrspTy());
    }
}