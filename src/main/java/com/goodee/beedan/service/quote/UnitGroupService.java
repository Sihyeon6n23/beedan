package com.goodee.beedan.service.quote;

import com.goodee.beedan.dto.quote.UnitGroupRequest;
import com.goodee.beedan.entity.UnitGroup;
import com.goodee.beedan.repository.quote.UnitGroupRepository;
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
public class UnitGroupService {

    private final UnitGroupRepository unitGroupRepository;

    @Transactional
    public UnitGroup create(UnitGroupRequest request) {

        if (unitGroupRepository.existsByUnGNm(request.getUnGNm())) {
            throw new IllegalStateException(
                    "이미 존재하는 단위명입니다: " + request.getUnGNm());
        }

        UnitGroup unitGroup = unitGroupRepository.save(
                UnitGroup.builder()
                        .unGNm(request.getUnGNm())
                        .unGQn(request.getUnGQn())
                        .build()
        );
        log.info("묶음 단위 생성 완료. ID: {}, 단위명: {}, 단위당 수량: {}",
                unitGroup.getUnGId(), unitGroup.getUnGNm(), unitGroup.getUnGQn());
        return unitGroup;
    }

    public UnitGroup findById(Long unGId) {
        return unitGroupRepository.findById(unGId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "묶음 단위를 찾을 수 없습니다. id: " + unGId));
    }

    public List<UnitGroup> findAllActive() {
        return unitGroupRepository.findAllByUnGYnTrue();
    }

    @Transactional
    public UnitGroup update(Long unGId, UnitGroupRequest request) {

        // 다른 단위에 같은 이름이 있으면 예외
        unitGroupRepository.findByUnGNm(request.getUnGNm())
                .ifPresent(existing -> {
                    if (!existing.getUnGId().equals(unGId)) {
                        throw new IllegalStateException(
                                "이미 존재하는 단위명입니다: " + request.getUnGNm());
                    }
                });

        UnitGroup unitGroup = findById(unGId);
        unitGroup.update(request.getUnGNm(), request.getUnGQn());
        log.info("묶음 단위 수정 완료. ID: {}, 단위명: {}, 단위당 수량: {}",
                unitGroup.getUnGId(), unitGroup.getUnGNm(), unitGroup.getUnGQn());
        return unitGroup;
    }

    @Transactional
    public void deactivate(Long unGId) {
        UnitGroup unitGroup = findById(unGId);
        unitGroup.deactivate();
        log.info("묶음 단위 비활성화 완료. ID: {}, 단위명: {}",
                unitGroup.getUnGId(), unitGroup.getUnGNm());
    }
}