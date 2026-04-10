package com.goodee.beedan.service.quote;

import com.goodee.beedan.common.constant.NotificationType;
import com.goodee.beedan.entity.*;
import com.goodee.beedan.repository.member.MemberRepository;
import com.goodee.beedan.repository.quote.QuoteSubmitCheckLogRepository;
import com.goodee.beedan.repository.quote.QuoteSubmitCheckRepository;
import com.goodee.beedan.service.mail.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteNotificationService {

    private final QuoteSubmitCheckLogRepository checkLogRepository;
    private final QuoteSubmitCheckRepository checkRepository;
    private final MemberRepository memberRepository;
    private final NegotiationService negotiationService;
    private final MailService mailService;

    /**
     * 특정 견적에서 해당 qscKey의 알림이 체크되었는지 확인
     */
    private boolean isChecked(Long quId, String qscKey) {
        // qscKey → qscId 조회
        QuoteSubmitCheck check = checkRepository.findAll().stream()
                .filter(c -> qscKey.equals(c.getQscKey()) && Boolean.TRUE.equals(c.getQscYn()))
                .findFirst().orElse(null);
        if (check == null) return false;

        // 체크 로그에서 해당 견적의 해당 항목이 체크되었는지 확인
        QuoteSubmitCheckLog logEntry = checkLogRepository.findByQuIdAndQscId(quId, check.getQscId());
        return logEntry != null && Boolean.TRUE.equals(logEntry.getQsclChkYn());
    }

    /**
     * 고객 이메일 조회 (협상의 memId 기준)
     */
    private String getCustomerEmail(Long ngId) {
        try {
            Long memId = negotiationService.getMemId(ngId);
            Member member = memberRepository.findById(memId).orElse(null);
            return member != null ? member.getMemEml() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 큐레이터가 견적을 확인했을 때 (emailOnReview)
     */
    public void notifyOnReview(QuoteBase quoteBase) {
        try {
            if (!isChecked(quoteBase.getQuId(), "emailOnReview")) return;
            String email = getCustomerEmail(quoteBase.getNgId());
            if (email == null) return;
            mailService.sendMail(email, NotificationType.QUOTATION_REVIEW, quoteBase.getQuId());
            log.info("견적 확인 알림 발송. quId: {}, email: {}", quoteBase.getQuId(), email);
        } catch (Exception e) {
            log.warn("견적 확인 알림 발송 실패: {}", e.getMessage());
        }
    }

    /**
     * 상대방이 견적을 회신(재작성/제출)했을 때 (emailOnQuoteReply)
     */
    public void notifyOnQuoteReply(Long originalQuId, Long ngId) {
        try {
            if (!isChecked(originalQuId, "emailOnQuoteReply")) return;
            String email = getCustomerEmail(ngId);
            if (email == null) return;
            mailService.sendMail(email, NotificationType.QUOTATION_REPLY, originalQuId);
            log.info("견적 회신 알림 발송. quId: {}, email: {}", originalQuId, email);
        } catch (Exception e) {
            log.warn("견적 회신 알림 발송 실패: {}", e.getMessage());
        }
    }

    /**
     * 견적이 승인되었을 때 (emailOnApprove)
     */
    public void notifyOnApprove(QuoteBase quoteBase) {
        try {
            if (!isChecked(quoteBase.getQuId(), "emailOnApprove")) return;
            String email = getCustomerEmail(quoteBase.getNgId());
            if (email == null) return;
            mailService.sendMail(email, NotificationType.QUOTATION_APPROVE, quoteBase.getQuId());
            log.info("견적 승인 알림 발송. quId: {}, email: {}", quoteBase.getQuId(), email);
        } catch (Exception e) {
            log.warn("견적 승인 알림 발송 실패: {}", e.getMessage());
        }
    }

    /**
     * 결제 완료 시 (emailOnPaid)
     */
    public void notifyOnPaid(QuoteBase quoteBase) {
        try {
            if (!isChecked(quoteBase.getQuId(), "emailOnPaid")) return;
            String email = getCustomerEmail(quoteBase.getNgId());
            if (email == null) return;
            mailService.sendMail(email, NotificationType.PAYMENT_COMPLETE, quoteBase.getQuId());
            log.info("결제 완료 알림 발송. quId: {}, email: {}", quoteBase.getQuId(), email);
        } catch (Exception e) {
            log.warn("결제 완료 알림 발송 실패: {}", e.getMessage());
        }
    }

    /**
     * 배송 상태 변경 시 (emailOnShipment)
     */
    public void notifyOnShipment(QuoteBase quoteBase) {
        try {
            if (!isChecked(quoteBase.getQuId(), "emailOnShipment")) return;
            String email = getCustomerEmail(quoteBase.getNgId());
            if (email == null) return;
            mailService.sendMail(email, NotificationType.DELIVERING_START, quoteBase.getQuId());
            log.info("배송 상태 알림 발송. quId: {}, email: {}", quoteBase.getQuId(), email);
        } catch (Exception e) {
            log.warn("배송 상태 알림 발송 실패: {}", e.getMessage());
        }
    }
}
