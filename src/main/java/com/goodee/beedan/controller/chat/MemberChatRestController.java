package com.goodee.beedan.controller.chat;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.chat.*;
import com.goodee.beedan.service.chat.MemberChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class MemberChatRestController {
    private final MemberChatService memberChatService;

    // 회원이 + 버튼으로 새 문의하기 시 기타 문의 채팅방 생성 또는 기존 활성방 반환
    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomOpenResultDto> openNewInquiryChatRoom(
            @AuthenticationPrincipal MemberUserDetails userDetails) {
        return ResponseEntity.ok(memberChatService.openNewInquiryChatRoom(userDetails.getMemberId()));
    }

    // 챗봇 최종 응답에서 상담 연결 시 기존 활성방 반환 또는 새 OPEN 채팅방 생성
    @PostMapping("/rooms/from-chatbot/{topicId}")
    public ResponseEntity<ChatRoomOpenResultDto> openChatRoomFromChatbot(
            @PathVariable Long topicId,
            @AuthenticationPrincipal MemberUserDetails userDetails) {
        return ResponseEntity.ok(memberChatService.openChatRoomFromChatbot(topicId, userDetails.getMemberId()));
    }

    // 로그인한 회원 본인의 채팅방 목록 조회
    @GetMapping("/rooms")
    public ResponseEntity<List<MemberChatRoomListDto>> getMemberChatRooms(
            @AuthenticationPrincipal MemberUserDetails userDetails) {
        return ResponseEntity.ok(memberChatService.getMemberChatRooms(userDetails.getMemberId()));
    }

    // 회원 채팅방 상세 조회
    @GetMapping("/rooms/{id}")
    public ResponseEntity<MemberChatRoomDetailDto> getMemberChatRoomDetail(
            @PathVariable("id") Long chRoId,
            @AuthenticationPrincipal MemberUserDetails userDetails) {
        return ResponseEntity.ok(memberChatService.getMemberChatRoomDetail(chRoId, userDetails.getMemberId()));
    }

    // 회원 채팅 메시지 전송 처리
    @PostMapping("/rooms/{id}/messages")
    public ResponseEntity<MemberChatMessageDto> sendMemberChatMessage(
            @PathVariable("id") Long chRoId,
            @AuthenticationPrincipal MemberUserDetails userDetails,
            @RequestBody MemberChatMessageSendDto request) {
        return ResponseEntity.ok(memberChatService.sendMemberChatMessage(chRoId, userDetails.getMemberId(), request.getChMsCon()));
    }

    // 회원 채팅 이미지 메시지 전송 처리
    @PostMapping("/rooms/{id}/image")
    public ResponseEntity<MemberChatMessageDto> sendMemberChatImage(
            @PathVariable("id") Long chRoId,
            @AuthenticationPrincipal MemberUserDetails userDetails,
            @ModelAttribute MemberChatImageMessageSendDto request) throws IOException {
        MemberChatMessageDto response = memberChatService.sendMemberChatImage(
                chRoId,
                userDetails.getMemberId(),
                request
        );
        return ResponseEntity.ok(response);
    }

    // 회원이 본인 채팅방 종료 처리
    @PatchMapping("/rooms/{id}/close")
    public ResponseEntity<Void> closeMemberChatRoom(
            @PathVariable("id") Long chRoId,
            @AuthenticationPrincipal MemberUserDetails userDetails) {
        memberChatService.closeMemberChatRoom(chRoId, userDetails.getMemberId());
        return ResponseEntity.ok().build();
    }

}
