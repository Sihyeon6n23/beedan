package com.goodee.beedan.service.quote;

import com.goodee.beedan.dto.quote.PortCustomsRateRequest;
import com.goodee.beedan.dto.quote.PortCustomsRateUpdateRequest;
import com.goodee.beedan.entity.PortCustomsRate;
import com.goodee.beedan.repository.quote.PortCustomsRateRepository;
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
public class PortCustomsRateService {

    private final PortCustomsRateRepository portCustomsRateRepository;

    /**
     * 항만비/통관수수료 등록
     * 같은 비용 종류가 이미 있으면 예외 발생
     */
    @Transactional
    public PortCustomsRate create(PortCustomsRateRequest request) {

        if (portCustomsRateRepository
                .findByPcrTyAndPcrYnTrue(request.getPcrTy())
                .isPresent()) {
            throw new IllegalStateException(
                    "이미 활성화된 정책이 존재합니다. 비용 종류: " + request.getPcrTy()
                            + " 수정을 원하시면 update를 사용하세요.");
        }

        PortCustomsRate portCustomsRate = portCustomsRateRepository.save(
                PortCustomsRate.builder()
                        .type(request.getPcrTy())
                        .smallAmount(request.getPcrSmAm())
                        .mediumAmount(request.getPcrMdAm())
                        .largeAmount(request.getPcrLgAm())
                        .description(request.getPcrDes())
                        .build()
        );
        log.info("항만비/통관수수료 등록 완료. ID: {}, 비용종류: {}",
                portCustomsRate.getPcrId(), portCustomsRate.getPcrTy());
        return portCustomsRate;
    }

    /**
     * 단건 조회
     */
    public PortCustomsRate findById(Long pcrId) {
        return portCustomsRateRepository.findById(pcrId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "항만비/통관수수료를 찾을 수 없습니다. id: " + pcrId));
    }

    /**
     * 비용 종류로 활성 정책 조회
     * 견적 계산 시 사용
     */
    public PortCustomsRate findActiveByType(String pcrTy) {
        return portCustomsRateRepository.findByPcrTyAndPcrYnTrue(pcrTy)
                .orElseThrow(() -> new EntityNotFoundException(
                        "활성화된 정책을 찾을 수 없습니다. 비용 종류: " + pcrTy));
    }

    /**
     * 활성 정책 전체 조회
     */
    public List<PortCustomsRate> findAllActive() {
        return portCustomsRateRepository.findAllByPcrYnTrue();
    }

    /**
     * 항만비/통관수수료 수정
     * 금액만 수정 가능 (비용 종류 변경 불가)
     */
    @Transactional
    public PortCustomsRate update(Long pcrId, PortCustomsRateUpdateRequest request) {
        PortCustomsRate portCustomsRate = findById(pcrId);
        portCustomsRate.update(
                request.getPcrSmAm(),
                request.getPcrMdAm(),
                request.getPcrLgAm(),
                request.getPcrDes()
        );
        log.info("항만비/통관수수료 수정 완료. ID: {}, 비용종류: {}",
                portCustomsRate.getPcrId(), portCustomsRate.getPcrTy());
        return portCustomsRate;
    }

    /**
     * 항만비/통관수수료 비활성화
     */
    @Transactional
    public void deactivate(Long pcrId) {
        PortCustomsRate portCustomsRate = findById(pcrId);
        portCustomsRate.deactivate();
        log.info("항만비/통관수수료 비활성화 완료. ID: {}, 비용종류: {}",
                portCustomsRate.getPcrId(), portCustomsRate.getPcrTy());
    }
}