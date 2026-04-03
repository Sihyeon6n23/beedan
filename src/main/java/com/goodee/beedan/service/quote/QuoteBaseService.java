package com.goodee.beedan.service.quote;

import com.goodee.beedan.common.constant.QuoteStatus;
import com.goodee.beedan.dto.quote.QuoteBaseRequest;
import com.goodee.beedan.entity.QuoteBase;
import com.goodee.beedan.repository.quote.QuoteBaseRepository;
import groovy.util.logging.Slf4j;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@lombok.extern.slf4j.Slf4j
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuoteBaseService {

    private final QuoteBaseRepository quoteBaseRepository;

    /**
     * 견적 생성
     * 생성 시 TEMP_SAVE 상태로 시작
     */
    @Transactional
    public QuoteBase create(QuoteBaseRequest request) {
        QuoteBase quoteBase = quoteBaseRepository.save(
                QuoteBase.builder()
                        .negoId(request.getNgId())
                        .senderId(request.getQuSid())
                        .receiverId(request.getQuRid())
                        .build()
        );
        log.info("견적 생성 완료. ID: {}, 협상ID: {}, 상태: {}",
                quoteBase.getQuId(), quoteBase.getNgId(), quoteBase.getQuStt());
        return quoteBase;
    }

    /**
     * 단건 조회
     */
    public QuoteBase findById(Long quId) {
        return quoteBaseRepository.findById(quId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "견적을 찾을 수 없습니다. id: " + quId));
    }

    /**
     * 전체 견적 조회
     */
    public List<QuoteBase> findAll() {
        return quoteBaseRepository.findAll();
    }

    /**
     * 협상별 견적 전체 조회
     */
    public List<QuoteBase> findAllByNego(Long ngId) {
        return quoteBaseRepository.findAllByNgId(ngId);
    }

    /**
     * 협상별 특정 상태 견적 조회
     */
    public List<QuoteBase> findAllByNegoAndStatus(Long ngId, QuoteStatus quStt) {
        return quoteBaseRepository.findAllByNgIdAndQuStt(ngId, quStt);
    }

    /**
     * 송신자별 견적 조회
     */
    public List<QuoteBase> findAllBySender(Long quSid) {
        return quoteBaseRepository.findAllByQuSid(quSid);
    }

    /**
     * 수신자별 견적 조회
     */
    public List<QuoteBase> findAllByReceiver(Long quRid) {
        return quoteBaseRepository.findAllByQuRid(quRid);
    }

    public Page<QuoteBase> findAllByReceiver(Long quRid, Pageable pageable) {
        return quoteBaseRepository.findAllByQuRid(quRid, pageable);
    }

    /**
     * 견적 제출
     * TEMP_SAVE 상태일 때만 가능
     */
    @Transactional
    public QuoteBase submit(Long quId) {
        QuoteBase quoteBase = findById(quId);
        if (!quoteBase.isEditable()) {
            throw new IllegalStateException(
                    "임시저장 상태의 견적만 제출할 수 있습니다. 현재 상태: "
                            + quoteBase.getQuStt());
        }
        quoteBase.submit();
        log.info("견적 제출 완료. ID: {}, 상태: {}", quId, quoteBase.getQuStt());
        return quoteBase;
    }

    /**
     * 견적 승인
     */
    @Transactional
    public QuoteBase approve(Long quId) {
        QuoteBase quoteBase = findById(quId);
        quoteBase.approve();
        log.info("견적 승인 완료. ID: {}, 상태: {}", quId, quoteBase.getQuStt());
        return quoteBase;
    }

    /**
     * 견적 거절
     */
    @Transactional
    public QuoteBase reject(Long quId, String reason) {
        QuoteBase quoteBase = findById(quId);
        quoteBase.reject(reason);
        log.info("견적 거절 완료. ID: {}, 상태: {}, 사유: {}",
                quId, quoteBase.getQuStt(), reason);
        return quoteBase;
    }

    /**
     * 견적 만료
     */
    @Transactional
    public QuoteBase expire(Long quId) {
        QuoteBase quoteBase = findById(quId);
        quoteBase.expire();
        log.info("견적 만료 처리 완료. ID: {}, 상태: {}", quId, quoteBase.getQuStt());
        return quoteBase;
    }

    /**
     * 운영자 열람 처리
     */
    @Transactional
    public QuoteBase adminOpen(Long quId) {
        QuoteBase quoteBase = findById(quId);
        quoteBase.adminOpened();
        log.info("운영자 열람 처리 완료. ID: {}", quId);
        return quoteBase;
    }

    /**
     * 사용자 열람 처리
     */
    @Transactional
    public QuoteBase userOpen(Long quId) {
        QuoteBase quoteBase = findById(quId);
        quoteBase.userOpened();
        log.info("사용자 열람 처리 완료. ID: {}", quId);
        return quoteBase;
    }
}