package com.goodee.beedan.quoteTests;

import com.goodee.beedan.dto.quote.NegotiationRequest;
import com.goodee.beedan.entity.Negotiation;
import com.goodee.beedan.repository.quote.NegotiationRepository;
import com.goodee.beedan.service.quote.NegotiationService;
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
class NegotiationTests {

    @Autowired
    private NegotiationRepository negotiationRepository;

    @Autowired
    private NegotiationService negotiationService;

    @Test
    @DisplayName("Negotiation CRUD 전체 흐름 확인")
    void negotiationCrudTest() {

        // ── 1. CREATE ─────────────────────────────────
        log.info("========== 1. CREATE ==========");

        NegotiationRequest request = new NegotiationRequest("테스트 협상", 1L);
        Negotiation created = negotiationService.create(request);

        log.info("생성된 협상 ID  : {}", created.getNgId());
        log.info("협상명          : {}", created.getNgNm());
        log.info("회원 아이디     : {}", created.getMemId());
        log.info("협상 시작 시간  : {}", created.getNgCreDt());
        log.info("협상 종료 시간  : {}", created.getNgEndDt());
        log.info("진행 중 여부    : {}", created.isOngoing());

        // ── 2. READ ───────────────────────────────────
        log.info("========== 2. READ ==========");

        Negotiation foundById = negotiationService.findById(created.getNgId());
        log.info("단건 조회       : {} / {}", foundById.getNgId(), foundById.getNgNm());

        List<Negotiation> allByMember = negotiationService.findAllByMember(1L);
        log.info("회원1 전체 협상 : {}개", allByMember.size());
        allByMember.forEach(n ->
                log.info("  └── ID: {}, 협상명: {}, 진행중: {}",
                        n.getNgId(), n.getNgNm(), n.isOngoing()));

        List<Negotiation> ongoingByMember = negotiationService.findOngoingByMember(1L);
        log.info("회원1 진행 중인 협상 : {}개", ongoingByMember.size());

        // ── 3. UPDATE (협상 종료) ─────────────────────
        log.info("========== 3. UPDATE (협상 종료) ==========");
        log.info("종료 전 진행 중 여부 : {}", created.isOngoing());
        log.info("종료 전 종료 시간    : {}", created.getNgEndDt());

        negotiationService.close(created.getNgId());

        log.info("종료 후 진행 중 여부 : {}", created.isOngoing());
        log.info("종료 후 종료 시간    : {}", created.getNgEndDt());

        List<Negotiation> ongoingAfterClose = negotiationService.findOngoingByMember(1L);
        log.info("종료 후 회원1 진행 중인 협상 : {}개", ongoingAfterClose.size());

        log.info("========== CRUD 전체 흐름 완료 ==========");
    }
}