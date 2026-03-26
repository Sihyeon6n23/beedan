package com.goodee.beedan.notificationTest;

import com.goodee.beedan.common.constant.NotificationType;
import com.goodee.beedan.entity.Notification;
import com.goodee.beedan.repository.notification.NotificationRepository;
import com.goodee.beedan.service.notification.NotificationService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@Transactional // 테스트 후 DB 롤백을 위해 필수
public class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    @DisplayName("알림 생성 로직 통합 테스트")
    void createNotificationIntegrationTest() {
        Long memId = 1L;
        //NotificationType type = NotificationType.ORDER_APPROVE;
        NotificationType type = NotificationType.INQUIRY_ANSWER;
        String detail = "12345번 주문";
        Long targetId = 100L;

        // 2. when: 메서드 실행
        notificationService.createNotification(memId, type, detail, targetId);

        // 3. then: DB에 실제로 저장되었는지 확인
        List<Notification> result = notificationRepository.findAll();
        // 가장 최근에 저장된 알림 확인
        Notification savedNoti = result.get(result.size() - 1);

        assertEquals("문의 답변 완료", savedNoti.getNotiTtl()); // 예상 제목 검증
        assertFalse(savedNoti.getNotiReaYn()); // 읽음 여부 기본값 FALSE 확인
    }
}
