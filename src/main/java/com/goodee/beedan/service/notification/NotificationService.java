package com.goodee.beedan.service.notification;

import com.goodee.beedan.common.constant.NotificationType;
import com.goodee.beedan.dto.notification.NotificationDto;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.entity.Notification;
import com.goodee.beedan.repository.member.MemberRepository;
import com.goodee.beedan.repository.notification.NotificationRepository;
import com.goodee.beedan.service.mail.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;

    private final MailService mailService;
    private final SimpMessagingTemplate messagingTemplate;

    public List<NotificationDto> getUnReadNotificationList(Long memId){
        memberRepository.getByIdOrThrow(memId);
        return notificationRepository.findAllNotDeletedByMemId(memId).stream().map(this::mapToNotificationDto).toList();
    }

    public List<NotificationDto> getNotificationList(Long memId, String filter){
        memberRepository.getByIdOrThrow(memId);

        List<Notification> notifications;
        switch (filter) {
            case "UNREAD"-> notifications = notificationRepository.findUnreadByMemId(memId);
            case "READ"-> notifications = notificationRepository.findReadByMemId(memId);
            default -> notifications = notificationRepository.findAllNotDeletedByMemId(memId);
        }

        return notifications.stream()
                .map(this::mapToNotificationDto)
                .toList();
    }

    /**
     * 인앱 알림만 생성 (이메일 발송 없음)
     */
    public void createInAppNotification(Long memId, NotificationType notiTp, Long targetId) {
        try {
            Member member = memberRepository.findById(memId).orElse(null);
            if (member == null) return;

            Notification notification = Notification.builder()
                    .member(member)
                    .notiTtl(notiTp.getDefaultTitle())
                    .notiCon(notiTp.getMessage())
                    .notiRef(notiTp.generateUrl(targetId))
                    .notiReaYn(false)
                    .notiDelYn(false)
                    .notiCreDt(LocalDateTime.now())
                    .build();
            notificationRepository.save(notification);

            sendRealTimeUnreadCount(memId);
        } catch (Exception e) {
            log.warn("인앱 알림 생성 실패: {}", e.getMessage());
        }
    }

    public void createNotification(Long memId, NotificationType notiTp, Long targetId){
        Member member = memberRepository.getByIdOrThrow(memId);

        String refUrl = notiTp.generateUrl(targetId);

        Notification notification = Notification.builder()
                .member(member)
                .notiTtl(notiTp.getDefaultTitle())
                .notiCon(notiTp.getMessage())
                .notiRef(refUrl)
                .notiReaYn(false)
                .notiDelYn(false)
                .notiCreDt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);

        mailService.sendMail(member.getMemEml(), notiTp, targetId);

        sendRealTimeUnreadCount(memId);
    }

    public void deleteNotification(Long notiId, Long memId) {
        memberRepository.getByIdOrThrow(memId);

        Notification notification = notificationRepository.getByIdOrThrow(notiId);
        notification.setNotiDelYn(true);

        sendRealTimeUnreadCount(memId);
    }

    public void deleteAll(Long memId){
        notificationRepository.updateAllDelYnByMemId(memId);

        sendRealTimeUnreadCount(memId);
    }

    public void readNotification(Long notiId, Long memId){
        memberRepository.getByIdOrThrow(memId);
        Notification notification = notificationRepository.getByIdOrThrow(notiId);
        notification.setNotiReaYn(true);

        sendRealTimeUnreadCount(memId);
    }

    public void readAll(Long memId){
        notificationRepository.updateAllReaYnByMemId(memId);

        sendRealTimeUnreadCount(memId);
    }

    public Integer sendRealTimeUnreadCount(Long memId) {
        Member member = memberRepository.getByIdOrThrow(memId);

        Integer currentUnreadCount = notificationRepository.countByMember_MemIdAndNotiReaYnFalseAndNotiDelYnFalse(memId);
        String destinationUser = String.valueOf(member.getMemLgnId());

        messagingTemplate.convertAndSendToUser(destinationUser, "/sub/unread-count", currentUnreadCount);

        return currentUnreadCount;
    }

    public Integer getUnreadCount(Long memId) {
        return notificationRepository.countByMember_MemIdAndNotiReaYnFalseAndNotiDelYnFalse(memId);
    }

    public NotificationDto mapToNotificationDto(Notification notification){
        return NotificationDto.builder()
                .notiId(notification.getNotiId())
                .notiTtl(notification.getNotiTtl())
                .notiCon(notification.getNotiCon())
                .notiReaYn(notification.getNotiReaYn())
                .notiCreDt(notification.getNotiCreDt())
                .notiUptDt(notification.getNotiUpdDt())
                .notiRef(notification.getNotiRef())
                .build();
    }
}
