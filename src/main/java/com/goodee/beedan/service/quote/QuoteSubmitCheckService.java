package com.goodee.beedan.service.quote;

import com.goodee.beedan.entity.QuoteSubmitCheck;
import com.goodee.beedan.entity.QuoteSubmitCheckLog;
import com.goodee.beedan.repository.quote.QuoteSubmitCheckRepository;
import com.goodee.beedan.repository.quote.QuoteSubmitCheckLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuoteSubmitCheckService {

    private final QuoteSubmitCheckRepository quoteSubmitCheckRepository;
    private final QuoteSubmitCheckLogRepository quoteSubmitCheckLogRepository;

    /**
     * 활성화된 체크 항목 조회 (필수 먼저, 정렬순)
     */
    public List<QuoteSubmitCheck> findAllActive() {
        return quoteSubmitCheckRepository.findAllByQscYnTrueOrderByQscRqYnDescQscSortAsc();
    }

    /**
     * 제출 시 체크 항목 동의 로그 저장
     * @param quId 견적 ID
     * @param checks key: qscId, value: 동의 여부
     */
    @Transactional
    public void saveCheckLog(Long quId, Map<Long, Boolean> checks) {
        checks.forEach((qscId, checked) -> {
            quoteSubmitCheckLogRepository.save(
                    QuoteSubmitCheckLog.builder()
                            .quId(quId)
                            .qscId(qscId)
                            .qsclChkYn(checked)
                            .build()
            );
        });
        log.info("제출 체크 로그 저장 완료. quId: {}, 항목 수: {}", quId, checks.size());
    }

    /**
     * 견적별 체크 로그 조회
     */
    public List<QuoteSubmitCheckLog> findLogsByQuId(Long quId) {
        return quoteSubmitCheckLogRepository.findAllByQuId(quId);
    }
}
