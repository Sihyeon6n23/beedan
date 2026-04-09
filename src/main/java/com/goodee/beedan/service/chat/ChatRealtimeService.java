package com.goodee.beedan.service.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRealtimeService {
    // 스프링이 제공하는 STOMP 메시지 발행 도구
    private final SimpMessagingTemplate messagingTemplate;

    // 특정 채팅방 구독자들에게 메시지를 실시간으로 push하는 메서드
    public void publishMessage(Long chRoId, Object payload) {
        messagingTemplate.convertAndSend("/sub/chat/rooms/" + chRoId, payload);
    }

    // 특정 채팅방 구독자들에게 채팅방 상태를 실시간으로 push하는 메서드(위 코드와 같지만 명시적으로 의미를 분리하기 위해)
    public void publishRoomStatus(Long chRoId, Object payload) {
        messagingTemplate.convertAndSend("/sub/chat/rooms/" + chRoId, payload);
    }

    // 특정 사용자 위젯에 채팅 목록/미읽음 표시를 다시 갱신하는 이벤트 전송 메서드
    public void publishMemberSummary(Long memId) {
        messagingTemplate.convertAndSend("/sub/chat/users/" + memId, "refresh");
    }

    // 관리자 채팅 목록 화면에 목록을 다시 갱신하는 이벤트 전송 메서드
    public void publishAdminSummary() {
        messagingTemplate.convertAndSend("/sub/chat/admin/summary", "refresh");
    }
}
