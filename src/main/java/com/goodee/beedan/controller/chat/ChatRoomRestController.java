package com.goodee.beedan.controller.chat;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.chat.ChatRoomOpenResultDto;
import com.goodee.beedan.service.chat.MemberChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatRoomRestController {
    private final MemberChatService chatRoomService;

    // 챗봇 최종 응답에서 상담사 연결 버튼을 누르면 활성 방을 반환하거나 새 OPEN 채팅방 생성
    @PostMapping("/rooms/from-chatbot/{topicId}")
    public ChatRoomOpenResultDto openChatRoomFromChatbot(
            @PathVariable Long topicId,
            @AuthenticationPrincipal MemberUserDetails userDetails
    ) {
        return chatRoomService.openChatRoomFromChatbot(topicId, userDetails.getMemberId());
    }
}
