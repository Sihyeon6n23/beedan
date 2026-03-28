package com.goodee.beedan.notificationTest;

import com.goodee.beedan.common.constant.MemberAuthority;
import com.goodee.beedan.common.constant.NotificationType;
import com.goodee.beedan.dto.notification.NotificationDto;
import com.goodee.beedan.entity.Member;
import com.goodee.beedan.entity.Notification;
import com.goodee.beedan.repository.member.MemberRepository;
import com.goodee.beedan.repository.notification.NotificationRepository;
import com.goodee.beedan.service.notification.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private MemberRepository memberRepository;

    @Test
    @DisplayName("알림 생성 로직 단위 테스트 - save 메서드 호출 검증")
    void createNotificationUnitTest() {
        // given
        Long memId = 1L;
        Member member = Member.builder().memId(memId).build();
        NotificationType type = NotificationType.INQUIRY_ANSWER;
        String detail = "12345번 주문";
        Long targetId = 100L;

        given(memberRepository.findById(memId)).willReturn(Optional.of(member));

        // when
        notificationService.createNotification(memId, type, detail, targetId);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(1)).save(captor.capture());

        Notification savedNoti = captor.getValue();
        assertEquals("문의 답변 완료", savedNoti.getNotiTtl());
        assertFalse(savedNoti.getNotiReaYn());
    }

    @Test
    @DisplayName("알림 읽음 처리 성공 - 상태값이 true로 변경되어야 함")
    void readNotification_Success() {
        // given
        Long notiId = 1L;
        Member member = Member.builder()
                .memId(1L)
                .memNm("테스트")
                .memLgnId("user")
                .memAut(MemberAuthority.USER)
                .build();

        Notification noti1 = Notification.builder()
                .notiId(notiId)
                .notiTtl("test title1")
                .notiReaYn(false)
                .member(member)
                .build();

        given(notificationRepository.findById(notiId)).willReturn(Optional.of(noti1));

        notificationService.readNotification(notiId, member.getMemId());

        assertTrue(noti1.getNotiReaYn(), "알림 상태가 true로 변경되어야 합니다.");
    }

    @Test
    @DisplayName("미확인 알림 목록 조회 성공 - DTO 리스트 반환")
    void getUnReadNotificationList_Success() {
        // given
        Long memId = 1L;
        Member member = Member.builder()
                .memId(1L)
                .memNm("테스트")
                .memLgnId("user")
                .build();

        Notification noti1 = Notification.builder().notiTtl("test title1").notiReaYn(false).member(member).build();
        Notification noti2 = Notification.builder().notiTtl("test title2").notiReaYn(false).member(member).build();

        given(memberRepository.findById(memId)).willReturn(Optional.of(member));
        given(notificationRepository.findUnReadAndNotDeleteListByMemId(memId))
                .willReturn(List.of(noti1, noti2));

        // when
        List<NotificationDto> result = notificationService.getUnReadNotificationList(memId);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("test title1", result.get(0).getNotiTtl());
    }
}