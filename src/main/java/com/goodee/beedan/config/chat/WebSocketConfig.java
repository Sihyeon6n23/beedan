package com.goodee.beedan.config.chat;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // WebSocket 메시지 브로커 기능(STOMP 기반 메시징) 활성화
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
                                        // 메시지 브로커 설정 커스터마이징을 위한 인터페이스
    // STOMP endpoint(진입점, 연결 지점) 등록
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws");
        // ws://localhost:8080/ws -> 맨 뒤의 "/ws"
    }

    // 메시지 송/수신 규칙 설정
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트가 구독(subscribe)해서 메시지를 수신 할 주소의 시작점 설정
        registry.enableSimpleBroker("/sub");
        // "/sub/..." : 서버가 뿌리는 출구

        // 클라이언트가 서버로 메시지를 송신(publish) 할 주소의 시작점 설정,
        registry.setApplicationDestinationPrefixes("/pub"); // 현재는 사용X
        // "/pub/..." : 서버로 보내는 입구
    }
}
