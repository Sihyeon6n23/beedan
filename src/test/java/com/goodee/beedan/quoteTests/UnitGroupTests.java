package com.goodee.beedan.quoteTests;

import com.goodee.beedan.dto.quote.UnitGroupRequest;
import com.goodee.beedan.entity.UnitGroup;
import com.goodee.beedan.repository.quote.UnitGroupRepository;
import com.goodee.beedan.service.quote.UnitGroupService;
import groovy.util.logging.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@lombok.extern.slf4j.Slf4j
@Slf4j
@SpringBootTest
@Transactional
class UnitGroupTests {

    @Autowired
    private UnitGroupRepository unitGroupRepository;

    @Autowired
    private UnitGroupService unitGroupService;

    @Test
    @DisplayName("UnitGroup CRUD 전체 흐름 확인")
    void unitGroupCrudTest() {

        // ── 1. CREATE ─────────────────────────────────
        log.info("========== 1. CREATE ==========");

        UnitGroupRequest createRequest =UnitGroupRequest.builder()
                .unGNm("박스")
                .unGQn(24)
                .build();
        UnitGroup created = unitGroupService.create(createRequest);

        log.info("생성된 단위 ID    : {}", created.getUnGId());
        log.info("단위명            : {}", created.getUnGNm());
        log.info("단위당 수량       : {}", created.getUnGQn());
        log.info("활성 여부         : {}", created.getUnGYn());
        log.info("생성일            : {}", created.getUnGCrDt());

        // ── 2. READ ───────────────────────────────────
        log.info("========== 2. READ ==========");

        UnitGroup foundById = unitGroupService.findById(created.getUnGId());
        log.info("단건 조회         : {} / {}개", foundById.getUnGNm(), foundById.getUnGQn());

        List<UnitGroup> allActive = unitGroupService.findAllActive();
        log.info("활성 단위 전체    : {}개", allActive.size());
        allActive.forEach(u ->
                log.info("  └── 단위명: {}, 단위당 수량: {}, 활성: {}",
                        u.getUnGNm(), u.getUnGQn(), u.getUnGYn()));

        // ── 3. calculateEachQty 확인 ──────────────────
        log.info("========== 3. calculateEachQty ==========");
        log.info("5타 × 10개 = {}개", created.calculateEachQty(5));
        log.info("0타 = {}개", created.calculateEachQty(0));
        log.info("null타 = {}개", created.calculateEachQty(null));

        // ── 4. UPDATE ─────────────────────────────────
        log.info("========== 4. UPDATE ==========");
        log.info("수정 전 단위명    : {}", created.getUnGNm());
        log.info("수정 전 단위당 수량: {}", created.getUnGQn());

        UnitGroupRequest updateRequest = new UnitGroupRequest("타스", 15);
        UnitGroup updated = unitGroupService.update(created.getUnGId(), updateRequest);

        log.info("수정 후 단위명    : {}", updated.getUnGNm());
        log.info("수정 후 단위당 수량: {}", updated.getUnGQn());

        // ── 5. 중복 단위명 예외 확인 ──────────────────
        log.info("========== 5. 중복 단위명 예외 확인 ==========");

        try {
            unitGroupService.create(new UnitGroupRequest("다스", 12));
        } catch (IllegalStateException e) {
            log.info("중복 단위명 예외 발생 확인: {}", e.getMessage());
        }

        // ── 6. DELETE (비활성화) ──────────────────────
        log.info("========== 6. DELETE (비활성화) ==========");
        log.info("비활성화 전 활성 여부: {}", updated.getUnGYn());

        unitGroupService.deactivate(updated.getUnGId());

        log.info("비활성화 후 활성 여부: {}", updated.getUnGYn());

        List<UnitGroup> afterDeactivate = unitGroupService.findAllActive();
        log.info("비활성화 후 활성 단위 수: {}개", afterDeactivate.size());

        log.info("========== CRUD 전체 흐름 완료 ==========");
    }
}