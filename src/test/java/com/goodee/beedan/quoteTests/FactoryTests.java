package com.goodee.beedan.quoteTests;

import com.goodee.beedan.dto.quote.FactoryRequest;
import com.goodee.beedan.entity.Factory;
import com.goodee.beedan.repository.quote.FactoryRepository;
import com.goodee.beedan.service.quote.FactoryService;
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
class FactoryTests {

    @Autowired
    private FactoryRepository factoryRepository;

    @Autowired
    private FactoryService factoryService;

    @Test
    @DisplayName("Factory CRUD 전체 흐름 확인")
    void factoryCrudTest() {

        // ── 1. CREATE ─────────────────────────────────
        log.info("========== 1. CREATE ==========");

        FactoryRequest createRequest = FactoryRequest.builder()
                .brId(1L)
                .faNm("나고야 1공장")
                .faAd("나고야 사카에 4-4")
                .faCty("나고야")
                .faCCd("JP")
                .build();

        Factory created = factoryService.create(createRequest);

        log.info("생성된 공장 ID  : {}", created.getFaId());
        log.info("브랜드 아이디   : {}", created.getBrId());
        log.info("공장명          : {}", created.getFaNm());
        log.info("공장 주소       : {}", created.getFaAd());
        log.info("공장 도시       : {}", created.getFaCty());
        log.info("국가코드        : {}", created.getFaCCd());
        log.info("활성 여부       : {}", created.getFaYn());

        // ── 2. READ ───────────────────────────────────
        log.info("========== 2. READ ==========");

        Factory foundById = factoryService.findById(created.getFaId());
        log.info("단건 조회       : {} / {}", foundById.getFaNm(), foundById.getFaCCd());

        List<Factory> allActive = factoryService.findAllActive();
        log.info("활성 공장 전체  : {}개", allActive.size());
        allActive.forEach(f ->
                log.info("  └── 공장명: {}, 국가: {}, 활성: {}",
                        f.getFaNm(), f.getFaCCd(), f.getFaYn()));

        List<Factory> jpFactories = factoryService.findAllByCountry("JP");
        log.info("일본 공장 수    : {}개", jpFactories.size());

        List<Factory> brandFactories = factoryService.findAllByBrand(1L);
        log.info("브랜드1 공장 수 : {}개", brandFactories.size());

        // ── 3. isSameCountry 확인 ─────────────────────
        log.info("========== 3. isSameCountry ==========");
        log.info("JP 공장인가 (JP): {}", created.isSameCountry("JP"));
        log.info("JP 공장인가 (CN): {}", created.isSameCountry("CN"));

        // ── 4. UPDATE ─────────────────────────────────
        log.info("========== 4. UPDATE ==========");
        log.info("수정 전 공장명  : {}", created.getFaNm());

        FactoryRequest updateRequest = FactoryRequest.builder()
                .brId(1L)
                .faNm("나고야 2공장")
                .faAd("나고야 사카에 5-5")
                .faCty("나고야")
                .faCCd("JP")
                .build();

        Factory updated = factoryService.update(created.getFaId(), updateRequest);

        log.info("수정 후 공장명  : {}", updated.getFaNm());
        log.info("수정 후 주소    : {}", updated.getFaAd());

        // ── 5. DELETE (비활성화) ──────────────────────
        log.info("========== 5. DELETE (비활성화) ==========");
        log.info("비활성화 전 활성 여부: {}", updated.getFaYn());

        factoryService.deactivate(updated.getFaId());

        log.info("비활성화 후 활성 여부: {}", updated.getFaYn());

        List<Factory> afterDeactivate = factoryService.findAllActive();
        log.info("비활성화 후 활성 공장 수: {}개", afterDeactivate.size());

        log.info("========== CRUD 전체 흐름 완료 ==========");
    }
}