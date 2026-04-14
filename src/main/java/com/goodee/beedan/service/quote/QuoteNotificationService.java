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

    // 알림 키 상수
    private static final String KEY_EMAIL_ON_REVIEW = "emailOnReview";
    private static final String KEY_EMAIL_ON_QUOTE_REPLY = "emailOnQuoteReply";
    private static final String KEY_EMAIL_ON_APPROVE = "emailOnApprove";
    private static final String KEY_EMAIL_ON_PAID = "emailOnPaid";
    private static final String KEY_EMAIL_ON_SHIPMENT = "emailOnShipment";

    private final QuoteSubmitCheckLogRepository checkLogRepository;
    private final QuoteSubmitCheckRepository checkRepository;
    private final MemberRepository memberRepository;
    private final NegotiationService negotiationService;
    private final MailService mailService;
    private final com.goodee.beedan.service.notification.NotificationService notificationService;

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
     * 고객 memId 조회 (협상의 memId 기준)
     */
    private Long getCustomerMemId(Long ngId) {
        try {
            return negotiationService.getMemId(ngId);
        } catch (Exception e) {
            log.debug("고객 memId 조회 실패 (ngId={}): {}", ngId, e.getMessage());
            return null;
        }
    }

    /**
     * 고객 이메일 조회 (협상의 memId 기준)
     */
    private String getCustomerEmail(Long ngId) {
        try {
            Long memId = getCustomerMemId(ngId);
            Member member = memId != null ? memberRepository.findById(memId).orElse(null) : null;
            return member != null ? member.getMemEml() : null;
        } catch (Exception e) {
            log.debug("고객 이메일 조회 실패 (ngId={}): {}", ngId, e.getMessage());
            return null;
        }
    }

    /**
     * 큐레이터가 견적을 확인했을 때 (emailOnReview)
     */
    public void notifyOnReview(QuoteBase quoteBase) {
        try {
            Long memId = getCustomerMemId(quoteBase.getNgId());
            // 인앱 알림 (항상)
            if (memId != null) {
                notificationService.createInAppNotification(memId, NotificationType.QUOTATION_REVIEW, quoteBase.getQuId());
            }
            // 이메일 (옵트인 시)
            if (isChecked(quoteBase.getQuId(), KEY_EMAIL_ON_REVIEW)) {
                String email = getCustomerEmail(quoteBase.getNgId());
                if (email != null) mailService.sendMail(email, NotificationType.QUOTATION_REVIEW, quoteBase.getQuId());
            }
        } catch (Exception e) {
            log.warn("견적 확인 알림 실패: {}", e.getMessage());
        }
    }

    /**
     * 상대방이 견적을 회신(재작성/제출)했을 때 (emailOnQuoteReply)
     */
    public void notifyOnQuoteReply(Long originalQuId, Long ngId) {
        try {
            Long memId = getCustomerMemId(ngId);
            if (memId != null) {
                notificationService.createInAppNotification(memId, NotificationType.QUOTATION_REPLY, originalQuId);
            }
            if (isChecked(originalQuId, KEY_EMAIL_ON_QUOTE_REPLY)) {
                String email = getCustomerEmail(ngId);
                if (email != null) mailService.sendMail(email, NotificationType.QUOTATION_REPLY, originalQuId);
            }
        } catch (Exception e) {
            log.warn("견적 회신 알림 실패: {}", e.getMessage());
        }
    }

    /**
     * 견적이 승인되었을 때 (emailOnApprove)
     */
    public void notifyOnApprove(QuoteBase quoteBase) {
        try {
            Long memId = getCustomerMemId(quoteBase.getNgId());
            if (memId != null) {
                notificationService.createInAppNotification(memId, NotificationType.QUOTATION_APPROVE, quoteBase.getQuId());
            }
            if (isChecked(quoteBase.getQuId(), KEY_EMAIL_ON_APPROVE)) {
                String email = getCustomerEmail(quoteBase.getNgId());
                if (email != null) mailService.sendMail(email, NotificationType.QUOTATION_APPROVE, quoteBase.getQuId());
            }
        } catch (Exception e) {
            log.warn("견적 승인 알림 실패: {}", e.getMessage());
        }
    }

    /**
     * 결제 완료 시 (emailOnPaid)
     */
    public void notifyOnPaid(QuoteBase quoteBase) {
        try {
            Long memId = getCustomerMemId(quoteBase.getNgId());
            if (memId != null) {
                notificationService.createInAppNotification(memId, NotificationType.PAYMENT_COMPLETE, quoteBase.getQuId());
            }
            if (isChecked(quoteBase.getQuId(), KEY_EMAIL_ON_PAID)) {
                String email = getCustomerEmail(quoteBase.getNgId());
                if (email != null) mailService.sendMail(email, NotificationType.PAYMENT_COMPLETE, quoteBase.getQuId());
            }
        } catch (Exception e) {
            log.warn("결제 완료 알림 실패: {}", e.getMessage());
        }
    }

    /**
     * 배송 상태 변경 시 (emailOnShipment)
     */
    public void notifyOnShipment(QuoteBase quoteBase) {
        try {
            Long memId = getCustomerMemId(quoteBase.getNgId());
            if (memId != null) {
                notificationService.createInAppNotification(memId, NotificationType.SHIPMENT_UPDATE, quoteBase.getQuId());
            }
            if (isChecked(quoteBase.getQuId(), KEY_EMAIL_ON_SHIPMENT)) {
                String email = getCustomerEmail(quoteBase.getNgId());
                if (email != null) mailService.sendMail(email, NotificationType.SHIPMENT_UPDATE, quoteBase.getQuId());
            }
        } catch (Exception e) {
            log.warn("배송 상태 알림 실패: {}", e.getMessage());
        }
    }
}
