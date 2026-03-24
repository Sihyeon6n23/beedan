package com.goodee.beedan.controller.chat;

import com.goodee.beedan.dto.chat.TestChatMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class TestChatController {
    // 주소용
    @GetMapping("/chat/test")
    public String chatTestPage() {
        return "chat-test/test-chat";
    }

    // 메시지 처리용 (나중에는 주소용 컨트롤러랑 처리용 컨트롤러를 분리하는 것을 권장)
    // 서버가 WebSocket/STOMP 구독자들에게 메시지를 보내기 위한 도구
    private final SimpMessagingTemplate messagingTemplate;

    // 클라이언트가 /pub/chat/message로 보낸 메시지를
    // 서버가 받아서 /sub/chat/rooms/{roomId}로 메시지를 뿌림
    // -> 그 roomId를 구독중인 클라이언트들이 메시지를 받음
    @MessageMapping("/chat/message") // config의 setApplicationDestinationPrefixes("/pub")이 "/pub"을 붙여줌
    public void sendMessage(TestChatMessageDto message) {                       // 그래서 "/pub/chat/message"
        // 객체를 메시지 형태로 변환해서 전송
        messagingTemplate.convertAndSend(
                "/sub/chat/rooms/" + message.getRoomId(),
                message
        );
    }
}
