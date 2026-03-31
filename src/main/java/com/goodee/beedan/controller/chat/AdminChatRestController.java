package com.goodee.beedan.controller.chat;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.chat.AdminChatMessageDto;
import com.goodee.beedan.dto.chat.AdminChatMessageSendDto;
import com.goodee.beedan.service.chat.AdminChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    // 채팅방 종료 상태(CLOSED)로 변경 처리
    @PatchMapping("/rooms/{id}/close")
    public void closeAdminChatRoom(@PathVariable("id") Long chRoId,
        @AuthenticationPrincipal MemberUserDetails userDetails) {
        adminChatService.closeAdminChatRoom(chRoId, userDetails.getMemberId());
    }

    // 채팅 전송 처리 (추후 Websocket 추가)
    @PostMapping("/rooms/{id}/messages")
    public AdminChatMessageDto sendAdminChatMessage(
            @PathVariable("id") Long chRoId,
            @AuthenticationPrincipal MemberUserDetails userDetails,
            @RequestBody AdminChatMessageSendDto request) {
        return adminChatService.sendAdminChatMessage(
                chRoId,
                userDetails.getMemberId(),
                request.getChMsCon()
        );
    }

}
