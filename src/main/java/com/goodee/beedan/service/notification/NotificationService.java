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

    public List<NotificationDto> getNotiList(Long memId){
        memberRepositroy.findById(memId).orElseThrow(()->new UsernameNotFoundException("Not user found"));
        List<NotificationDto> notiList = notificationRepository.findByMember_MemIdAndNotiDelYnFalseOrderByNotiCreDtDesc(memId)
                .stream()
                .map(notification -> mapToNotificationDto(notification))
                .toList();

        return notiList;
    }

    public void createNoti(Long memId, NotificationType notiTp, String detail, Long targetId){
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

        log.info("알림 생성 테스트: {}", notification.toString());

        notificationRepository.save(notification);
    }

    public NotificationDto mapToNotificationDto(Notification notification){
        return NotificationDto.builder()
                .notiId(notification.getNotiId())
                .notiTtl(notification.getNotiTtl())
                .notiCon(notification.getNotiCon())
                .notiReaYn(notification.getNotiReaYn())
                .notiCreAt(notification.getNotiCreDt())
                .notiUptDt(notification.getNotiUpdDt())
                .build();
    }

    public void deleteNoti(Long notiId) {
        Notification notification = notificationRepository.findById(notiId).orElseThrow(() -> new IllegalArgumentException("Can't find notice"));
        notification.setNotiDelYn(true);
    }

    public String updateNotiReaYn(Long notiId){
        Notification notification = notificationRepository.findById(notiId).orElseThrow(()->new IllegalArgumentException("Can't find notice"));

        notification.setNotiReaYn(true);

        return notification.getNotiRef();
    }

    public int updateAllNotiReaYn(Long memId){
        return notificationRepository.updateAllRedYnByMemId(memId);
    }

    @Transactional(readOnly = true)
    public int getUnreadCount(Long memId) {
        return notificationRepository.countByMember_MemIdAndNotiReaYnFalse(memId);
    }

    public void updateNoti(Long notiId, Long upd_mem_id, NotificationDto updateDto){
        Notification notification = notificationRepository.findById(notiId).orElseThrow(()-> new IllegalArgumentException("Can't find notice"));

        notification.setNotiTtl(updateDto.getNotiTtl());
        notification.setNotiCon(updateDto.getNotiCon());
        notification.setNotiUpdMemId(upd_mem_id);
        notification.setNotiUpdDt(LocalDateTime.now());
    }

}
