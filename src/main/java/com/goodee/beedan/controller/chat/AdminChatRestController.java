package com.goodee.beedan.controller.chat;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.service.chat.AdminChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/chat")
public class AdminChatRestController {
    private final AdminChatService adminChatService;

    // 채팅방 시작 상태(ONGOING)로 변경 처리
    @PatchMapping("/rooms/{id}/start")
    public void startAdminChatRoom(@PathVariable("id") Long chRoId,
        @AuthenticationPrincipal MemberUserDetails userDetails) {
        adminChatService.startAdminChatRoom(chRoId, userDetails.getMemberId());
    }
}
