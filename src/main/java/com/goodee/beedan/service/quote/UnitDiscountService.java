package com.goodee.beedan.service.quote;

import com.goodee.beedan.dto.quote.UnitDiscountRequest;
import com.goodee.beedan.entity.UnitDiscount;
import com.goodee.beedan.repository.quote.UnitDiscountRepository;
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
public class UnitDiscountService {

    private final UnitDiscountRepository unitDiscountRepository;

    @Transactional
    public UnitDiscount create(UnitDiscountRequest request) {
        UnitDiscount unitDiscount = unitDiscountRepository.save(
                UnitDiscount.builder()
                        .unitGroupId(request.getUnGId())
                        .minQuantity(request.getUnDMinQn())
                        .minAmount(request.getUnDMinAm())
                        .quantityDiscountRate(request.getUnDQnDr())
                        .amountDiscountRate(request.getUnDAmDr())
                        .overlapType(request.getUnDOvTy())
                        .description(request.getUnDDes())
                        .build()
        );
        log.info("묶음 할인 정책 생성 완료. ID: {}, 묶음단위: {}, 처리방식: {}",
                unitDiscount.getUnDId(), unitDiscount.getUnGId(), unitDiscount.getUnDOvTy());
        return unitDiscount;
    }

    public UnitDiscount findById(Long unDId) {
        return unitDiscountRepository.findById(unDId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "묶음 할인 정책을 찾을 수 없습니다. id: " + unDId));
    }

    public List<UnitDiscount> findAllByUnitGroup(Long unGId) {
        return unitDiscountRepository.findAllByUnGId(unGId);
    }

    /**
     * 묶음 할인 정책 수정
     */
    @Transactional
    public UnitDiscount update(Long unDId, UnitDiscountRequest request) {
        UnitDiscount unitDiscount = findById(unDId);

        // 기존 삭제 후 새로 생성 (이력 보존)
        unitDiscountRepository.delete(unitDiscount);

        UnitDiscount updated = unitDiscountRepository.save(
                UnitDiscount.builder()
                        .unitGroupId(request.getUnGId())
                        .minQuantity(request.getUnDMinQn())
                        .minAmount(request.getUnDMinAm())
                        .quantityDiscountRate(request.getUnDQnDr())
                        .amountDiscountRate(request.getUnDAmDr())
                        .overlapType(request.getUnDOvTy())
                        .description(request.getUnDDes())
                        .build()
        );
        log.info("묶음 할인 정책 수정 완료. 기존 ID: {}, 새 ID: {}",
                unDId, updated.getUnDId());
        return updated;
    }

    /**
     * 묶음 할인 정책 삭제
     */
    @Transactional
    public void delete(Long unDId) {
        UnitDiscount unitDiscount = findById(unDId);
        unitDiscountRepository.delete(unitDiscount);
        log.info("묶음 할인 정책 삭제 완료. ID: {}", unDId);
    }

    /**
     * 최종 할인율 계산
     * 견적 계산 시 사용
     */
    public BigDecimal calculateDiscount(Long unGId,
                                        Integer unitCount,
                                        BigDecimal totalAmount) {
        List<UnitDiscount> policies = findAllByUnitGroup(unGId);

        return policies.stream()
                .map(p -> p.calculateFinalDiscountRate(unitCount, totalAmount))
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }
}