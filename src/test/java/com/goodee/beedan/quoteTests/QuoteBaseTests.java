package com.goodee.beedan.quoteTests;

import com.goodee.beedan.common.constant.QuoteStatus;
import com.goodee.beedan.dto.quote.QuoteBaseRequest;
import com.goodee.beedan.entity.QuoteBase;
import com.goodee.beedan.repository.quote.QuoteBaseRepository;
import com.goodee.beedan.service.quote.QuoteBaseService;
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
class QuoteBaseTests {

    @Autowired
    private QuoteBaseRepository quoteBaseRepository;

    @Autowired
    private QuoteBaseService quoteBaseService;

    @Test
    @DisplayName("QuoteBase CRUD 전체 흐름 확인")
    void quoteBaseCrudTest() {

        // ── 1. CREATE ─────────────────────────────────
        log.info("========== 1. CREATE ==========");

        QuoteBaseRequest createRequest = QuoteBaseRequest.builder()
                .ngId(1L)
                .quSid(1L)
                .quRid(2L)
                .build();

        QuoteBase created = quoteBaseService.create(createRequest);

        log.info("생성된 견적 ID  : {}", created.getQuId());
        log.info("협상 아이디     : {}", created.getNgId());
        log.info("송신자 아이디   : {}", created.getQuSid());
        log.info("수신자 아이디   : {}", created.getQuRid());
        log.info("견적 상태       : {}", created.getQuStt());
        log.info("열람 여부       : {}", created.getQuOpYn());
        log.info("수정 가능 여부  : {}", created.isEditable());
        log.info("만료 여부       : {}", created.isExpired());

        // ── 2. READ ───────────────────────────────────
        log.info("========== 2. READ ==========");

        QuoteBase foundById = quoteBaseService.findById(created.getQuId());
        log.info("단건 조회       : ID={}, 상태={}", foundById.getQuId(), foundById.getQuStt());

        List<QuoteBase> allByNego = quoteBaseService.findAllByNego(1L);
        log.info("협상1 견적 수   : {}개", allByNego.size());
        allByNego.forEach(q ->
                log.info("  └── ID: {}, 상태: {}, 열람: {}",
                        q.getQuId(), q.getQuStt(), q.getQuOpYn()));

        List<QuoteBase> tempSaves = quoteBaseService
                .findAllByNegoAndStatus(1L, QuoteStatus.TEMP_SAVE);
        log.info("협상1 임시저장 견적: {}개", tempSaves.size());

        // ── 3. 상태 변경 흐름 ─────────────────────────
        log.info("========== 3. 상태 변경 흐름 ==========");

        // 제출
        log.info("제출 전 상태    : {}", created.getQuStt());
        QuoteBase submitted = quoteBaseService.submit(created.getQuId());
        log.info("제출 후 상태    : {}", submitted.getQuStt());
        log.info("제출 후 수정 가능 여부: {}", submitted.isEditable());

        // 승인
        QuoteBase approved = quoteBaseService.approve(created.getQuId());
        log.info("승인 후 상태    : {}", approved.getQuStt());

        // ── 4. 열람 처리 ──────────────────────────────
        log.info("========== 4. 열람 처리 ==========");
        log.info("열람 전         : {}", created.getQuOpYn());

        quoteBaseService.open(created.getQuId());
        log.info("열람 후         : {}", created.getQuOpYn());

        // ── 5. 거절 흐름 ──────────────────────────────
        log.info("========== 5. 거절 흐름 ==========");

        QuoteBase newQuote = quoteBaseService.create(createRequest);
        quoteBaseService.submit(newQuote.getQuId());
        QuoteBase rejected = quoteBaseService.reject(newQuote.getQuId(), "가격이 맞지 않습니다");
        log.info("거절 후 상태    : {}", rejected.getQuStt());
        log.info("거절 사유       : {}", rejected.getQuCon());

        // ── 6. 만료 처리 ──────────────────────────────
        log.info("========== 6. 만료 처리 ==========");

        QuoteBase expiredQuote = quoteBaseService.create(createRequest);
        quoteBaseService.expire(expiredQuote.getQuId());
        log.info("만료 후 상태    : {}", expiredQuote.getQuStt());

        log.info("========== CRUD 전체 흐름 완료 ==========");
    }
}