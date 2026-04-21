package com.goodee.beedan.controller.chat;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.chat.AdminChatImageMessageSendDto;
import com.goodee.beedan.dto.chat.AdminChatMessageDto;
import com.goodee.beedan.dto.chat.AdminChatMessageSendDto;
import com.goodee.beedan.dto.chat.AdminChatQuoteCardMessageSendDto;
import com.goodee.beedan.service.chat.AdminChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/chat")
public class AdminChatRestController {
    private final AdminChatService adminChatService;

    // 채팅방 시작 상태(ONGOING)로 변경 처리
    @PatchMapping("/rooms/{id}/start")
    public ResponseEntity<Void> startAdminChatRoom(
            @PathVariable("id") Long chRoId,
            @AuthenticationPrincipal MemberUserDetails userDetails) {
        adminChatService.startAdminChatRoom(chRoId, userDetails.getMemberId());
        return ResponseEntity.ok().build();
    }

    // 채팅방 종료 상태(CLOSED)로 변경 처리
    @PatchMapping("/rooms/{id}/close")
    public ResponseEntity<Void> closeAdminChatRoom(
            @PathVariable("id") Long chRoId,
            @AuthenticationPrincipal MemberUserDetails userDetails) {
        adminChatService.closeAdminChatRoom(chRoId, userDetails.getMemberId());
        return ResponseEntity.ok().build();
    }

    // 채팅 메시지 전송 처리
    @PostMapping("/rooms/{id}/messages")
    public ResponseEntity<AdminChatMessageDto> sendAdminChatMessage(
            @PathVariable("id") Long chRoId,
            @AuthenticationPrincipal MemberUserDetails userDetails,
            @RequestBody AdminChatMessageSendDto request) {
        AdminChatMessageDto response = adminChatService.sendAdminChatMessage(
                chRoId,
                userDetails.getMemberId(),
                request.getChMsCon()
        );
        return ResponseEntity.ok(response);
    }

    // 채팅 이미지 메시지 전송 처리
    @PostMapping("/rooms/{id}/image")
    public ResponseEntity<AdminChatMessageDto> sendAdminChatImage(
            @PathVariable("id") Long chRoId,
            @AuthenticationPrincipal MemberUserDetails userDetails,
            @ModelAttribute AdminChatImageMessageSendDto request) throws IOException {
        AdminChatMessageDto response = adminChatService.sendAdminChatImage(
                chRoId,
                userDetails.getMemberId(),
                request
        );
        return ResponseEntity.ok(response);
    }

    // 채팅 견적 카드 메시지 전송 처리
    @PostMapping("/rooms/{id}/quote-card")
    public ResponseEntity<AdminChatMessageDto> sendAdminQuoteCard(
            @PathVariable("id") Long chRoId,
            @AuthenticationPrincipal MemberUserDetails userDetails,
            @RequestBody AdminChatQuoteCardMessageSendDto request) {
        AdminChatMessageDto response = adminChatService.sendAdminQuoteCard(
                chRoId,
                userDetails.getMemberId(),
                request
        );
        return ResponseEntity.ok(response);
    }
}
