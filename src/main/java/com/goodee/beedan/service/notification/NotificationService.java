package com.goodee.beedan.service.notification;

import com.goodee.beedan.common.constant.NotificationType;
import com.goodee.beedan.dto.notification.NotificationDto;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.entity.Notification;
import com.goodee.beedan.repository.member.MemberRepository;
import com.goodee.beedan.repository.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    private final MemberRepository memberRepositroy;

    public List<NotificationDto> getUnReadNotificationList(Long memId){
        memberRepositroy.findById(memId).orElseThrow(()->new UsernameNotFoundException("Not user found"));
        List<NotificationDto> notificationDtoList = notificationRepository
                .findAllNotDeletedByMemId(memId)
                .stream()
                .map(notification -> mapToNotificationDto(notification))
                .toList();

        return notificationDtoList;
    }

    public void createNotification(Long memId, NotificationType notiTp, String detail, Long targetId){
        Member member = memberRepositroy.findById(memId).orElseThrow(()-> new UsernameNotFoundException("User not found"));

        String content = notiTp.generateContent(detail);
        String refUrl = notiTp.generateUrl(targetId);

        Notification notification = Notification.builder()
                .member(member)
                .notiTtl(notiTp.getDefaultTitle())
                .notiCon(content)
                .notiRef(refUrl)
                .notiReaYn(false)
                .notiDelYn(false)
                .notiCreDt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
    }

    public void deleteNotification(Long notiId, Long memId) {
        memberRepositroy.findById(memId).orElseThrow(()->new UsernameNotFoundException("Not user found"));

        Notification notification = notificationRepository.findById(notiId).orElseThrow(() -> new IllegalArgumentException("Can't find notice"));
        notification.setNotiDelYn(true);
    }

    public void deleteAll(Long memId){ notificationRepository.updateAllDelYnByMemId(memId); }

    public void readNotification(Long notiId, Long memId){
        memberRepositroy.findById(memId).orElseThrow(()->new UsernameNotFoundException("Not user found"));

        Notification notification = notificationRepository.findById(notiId).orElseThrow(()->new IllegalArgumentException("Can't find notice"));
        notification.setNotiReaYn(true);
    }

    public void readAll(Long memId){ notificationRepository.updateAllReaYnByMemId(memId); }

    @Transactional(readOnly = true)
    public int getUnreadCount(Long memId) {
        return notificationRepository.countByMember_MemIdAndNotiReaYnFalseAndNotiDelYnFalse(memId);
    }

    public void changeNoti(Long notiId, Long upd_mem_id, NotificationDto notificationDto){
        Notification notification = notificationRepository.findById(notiId).orElseThrow(()-> new IllegalArgumentException("Can't find notice"));

        if(notificationDto.getNotiTtl() != null) notification.setNotiTtl(notificationDto.getNotiTtl());
        if(notificationDto.getNotiCon() != null) notification.setNotiCon(notificationDto.getNotiCon());

        notification.setNotiUpdMemId(upd_mem_id);
        notification.setNotiUpdDt(LocalDateTime.now());
    }

    public NotificationDto mapToNotificationDto(Notification notification){
        return NotificationDto.builder()
                .notiId(notification.getNotiId())
                .notiTtl(notification.getNotiTtl())
                .notiCon(notification.getNotiCon())
                .notiReaYn(notification.getNotiReaYn())
                .notiCreDt(notification.getNotiCreDt())
                .notiUptDt(notification.getNotiUpdDt())
                .build();
    }

}
