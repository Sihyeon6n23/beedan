package com.goodee.beedan.notificationTests;

import com.goodee.beedan.common.constant.MemberAuthority;
import com.goodee.beedan.common.constant.NotificationType;
import com.goodee.beedan.dto.notification.NotificationDto;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.entity.Notification;
import com.goodee.beedan.repository.member.MemberRepository;
import com.goodee.beedan.repository.notification.NotificationRepository;
import com.goodee.beedan.service.notification.NotificationService;
import com.goodee.beedan.service.mail.MailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTests {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MailService mailService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private Member member;
    private Notification notification;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .memId(1L)
                .memNm("테스트 사용자")
                .memLgnId("testuser")
                .memEml("test@example.com")
                .memAut(MemberAuthority.USER)
                .build();

        notification = Notification.builder()
                .notiId(1L)
                .notiTtl("테스트 알림")
                .notiCon("테스트 알림 내용")
                .notiRef("/test/ref/1")
                .notiReaYn(false)
                .notiDelYn(false)
                .notiCreDt(LocalDateTime.now())
                .member(member)
                .build();
    }

    @Test
    @DisplayName("알림 생성 성공 - 메일 발송 및 WebSocket 메시지 전송 확인")
    void createNotification_Success() {
        // given
        Long memId = 1L;
        NotificationType type = NotificationType.INQUIRY_ANSWER_CREATE;
        Long targetId = 100L;

        given(memberRepository.getByIdOrThrow(memId)).willReturn(member);
        given(notificationRepository.countByMember_MemIdAndNotiReaYnFalseAndNotiDelYnFalse(memId)).willReturn(1);
        willDoNothing().given(mailService).sendMail(anyString(), any(NotificationType.class), any(Long.class));
        willDoNothing().given(messagingTemplate).convertAndSendToUser(anyString(), anyString(), any());

        // when
        notificationService.createNotification(memId, type, targetId);

        // then
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(captor.capture());
        verify(mailService, times(1)).sendMail(eq(member.getMemEml()), eq(type), eq(targetId));
        verify(messagingTemplate, times(1)).convertAndSendToUser(anyString(), eq("/sub/unread-count"), any());

        Notification savedNoti = captor.getValue();
        assertEquals("문의 답변 등록", savedNoti.getNotiTtl());
        assertFalse(savedNoti.getNotiReaYn());
        assertFalse(savedNoti.getNotiDelYn());
    }

    @Test
    @DisplayName("인앱 알림만 생성 - 메일 발송 없음")
    void createInAppNotification_Success() {
        // given
        Long memId = 1L;
        NotificationType type = NotificationType.INQUIRY_ANSWER_CREATE;
        Long targetId = 100L;

        given(memberRepository.findById(memId)).willReturn(Optional.of(member));
        given(memberRepository.getByIdOrThrow(memId)).willReturn(member);
        given(notificationRepository.countByMember_MemIdAndNotiReaYnFalseAndNotiDelYnFalse(memId)).willReturn(1);
        willDoNothing().given(messagingTemplate).convertAndSendToUser(anyString(), anyString(), any());

        // when
        notificationService.createInAppNotification(memId, type, targetId);

        // then
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(captor.capture());
        verify(mailService, never()).sendMail(anyString(), any(NotificationType.class), any(Long.class));
        verify(messagingTemplate, times(1)).convertAndSendToUser(anyString(), eq("/sub/unread-count"), any());

        Notification savedNoti = captor.getValue();
        assertFalse(savedNoti.getNotiReaYn());
    }

    @Test
    @DisplayName("알림 읽음 처리 성공 - 상태값이 true로 변경되어야 함")
    void readNotification_Success() {
        // given
        Long notiId = 1L;
        Long memId = 1L;

        given(memberRepository.getByIdOrThrow(memId)).willReturn(member);
        given(notificationRepository.getByIdOrThrow(notiId)).willReturn(notification);

        // when
        notificationService.readNotification(notiId, memId);

        // then
        assertTrue(notification.getNotiReaYn(), "알림 상태가 true로 변경되어야 합니다.");
    }

    @Test
    @DisplayName("모든 알림 읽음 처리 성공")
    void readAll_Success() {
        // given
        Long memId = 1L;

        given(memberRepository.getByIdOrThrow(memId)).willReturn(member);
        given(notificationRepository.countByMember_MemIdAndNotiReaYnFalseAndNotiDelYnFalse(memId)).willReturn(0);
        willDoNothing().given(notificationRepository).updateAllReaYnByMemId(memId);
        willDoNothing().given(messagingTemplate).convertAndSendToUser(anyString(), anyString(), any());

        // when
        notificationService.readAll(memId);

        // then
        verify(notificationRepository, times(1)).updateAllReaYnByMemId(memId);
        verify(messagingTemplate, times(1)).convertAndSendToUser(eq("testuser"), eq("/sub/unread-count"), eq(0));
    }

    @Test
    @DisplayName("미확인 알림 목록 조회 성공 - DTO 리스트 반환")
    void getUnReadNotificationList_Success() {
        // given
        Long memId = 1L;

        Notification noti1 = Notification.builder()
                .notiId(1L)
                .notiTtl("test title1")
                .notiReaYn(false)
                .member(member)
                .build();
        Notification noti2 = Notification.builder()
                .notiId(2L)
                .notiTtl("test title2")
                .notiReaYn(false)
                .member(member)
                .build();

        given(memberRepository.getByIdOrThrow(memId)).willReturn(member);
        given(notificationRepository.findAllNotDeletedByMemId(memId))
                .willReturn(List.of(noti1, noti2));

        // when
        List<NotificationDto> result = notificationService.getUnReadNotificationList(memId);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("test title1", result.get(0).getNotiTtl());
        assertEquals("test title2", result.get(1).getNotiTtl());
        verify(memberRepository, times(1)).getByIdOrThrow(memId);
    }

    @Test
    @DisplayName("필터를 적용한 알림 목록 조회 - UNREAD 필터")
    void getNotificationList_WithUnreadFilter() {
        // given
        Long memId = 1L;
        String filter = "UNREAD";

        Notification unreadNoti = Notification.builder()
                .notiId(1L)
                .notiTtl("미읽음 알림")
                .notiReaYn(false)
                .member(member)
                .build();

        given(memberRepository.getByIdOrThrow(memId)).willReturn(member);
        given(notificationRepository.findUnreadByMemId(memId))
                .willReturn(List.of(unreadNoti));

        // when
        List<NotificationDto> result = notificationService.getNotificationList(memId, filter);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.get(0).getNotiReaYn());
    }

    @Test
    @DisplayName("필터를 적용한 알림 목록 조회 - READ 필터")
    void getNotificationList_WithReadFilter() {
        // given
        Long memId = 1L;
        String filter = "READ";

        Notification readNoti = Notification.builder()
                .notiId(1L)
                .notiTtl("읽은 알림")
                .notiReaYn(true)
                .member(member)
                .build();

        given(memberRepository.getByIdOrThrow(memId)).willReturn(member);
        given(notificationRepository.findReadByMemId(memId))
                .willReturn(List.of(readNoti));

        // when
        List<NotificationDto> result = notificationService.getNotificationList(memId, filter);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getNotiReaYn());
    }

    @Test
    @DisplayName("알림 삭제 성공 - 삭제 플래그 설정 및 WebSocket 메시지 전송")
    void deleteNotification_Success() {
        // given
        Long notiId = 1L;
        Long memId = 1L;

        given(memberRepository.getByIdOrThrow(memId)).willReturn(member);
        given(notificationRepository.getByIdOrThrow(notiId)).willReturn(notification);
        given(notificationRepository.countByMember_MemIdAndNotiReaYnFalseAndNotiDelYnFalse(memId)).willReturn(0);
        willDoNothing().given(messagingTemplate).convertAndSendToUser(anyString(), anyString(), any());

        // when
        notificationService.deleteNotification(notiId, memId);

        // then
        assertTrue(notification.getNotiDelYn(), "알림이 삭제되어야 합니다.");
        verify(messagingTemplate, times(1)).convertAndSendToUser(anyString(), eq("/sub/unread-count"), any());
    }

    @Test
    @DisplayName("모든 알림 삭제 성공")
    void deleteAll_Success() {
        // given
        Long memId = 1L;

        given(memberRepository.getByIdOrThrow(memId)).willReturn(member);
        given(notificationRepository.countByMember_MemIdAndNotiReaYnFalseAndNotiDelYnFalse(memId)).willReturn(0);
        willDoNothing().given(notificationRepository).updateAllDelYnByMemId(memId);
        willDoNothing().given(messagingTemplate).convertAndSendToUser(anyString(), anyString(), any());

        // when
        notificationService.deleteAll(memId);

        // then
        verify(notificationRepository, times(1)).updateAllDelYnByMemId(memId);
        verify(messagingTemplate, times(1)).convertAndSendToUser(eq("testuser"), eq("/sub/unread-count"), eq(0));
    }

    @Test
    @DisplayName("실시간 미읽음 알림 개수 전송")
    void sendRealTimeUnreadCount_Success() {
        // given
        Long memId = 1L;
        Integer unreadCount = 5;

        given(memberRepository.getByIdOrThrow(memId)).willReturn(member);
        given(notificationRepository.countByMember_MemIdAndNotiReaYnFalseAndNotiDelYnFalse(memId))
                .willReturn(unreadCount);
        willDoNothing().given(messagingTemplate).convertAndSendToUser(anyString(), anyString(), any());

        // when
        Integer result = notificationService.sendRealTimeUnreadCount(memId);

        // then
        assertEquals(unreadCount, result);
        verify(messagingTemplate, times(1))
                .convertAndSendToUser(eq("testuser"), eq("/sub/unread-count"), eq(unreadCount));
    }

    @Test
    @DisplayName("DTO 매핑 테스트")
    void mapToNotificationDto_Success() {
        // when
        NotificationDto dto = notificationService.mapToNotificationDto(notification);

        // then
        assertNotNull(dto);
        assertEquals(notification.getNotiId(), dto.getNotiId());
        assertEquals(notification.getNotiTtl(), dto.getNotiTtl());
        assertEquals(notification.getNotiCon(), dto.getNotiCon());
        assertEquals(notification.getNotiReaYn(), dto.getNotiReaYn());
        assertEquals(notification.getNotiRef(), dto.getNotiRef());
    }
}