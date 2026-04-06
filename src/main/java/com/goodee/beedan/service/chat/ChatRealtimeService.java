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
}
