package com.goodee.beedan.controller.chat;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.chat.*;
import com.goodee.beedan.service.chat.MemberChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class MemberChatRestController {
    private final MemberChatService memberChatService;

    // 챗봇 최종 응답에서 상담사 연결 시 기존 활성 방을 반환하거나 새 OPEN 채팅방을 생성
    @PostMapping("/rooms/from-chatbot/{topicId}")
    public ChatRoomOpenResultDto openChatRoomFromChatbot(
            @PathVariable Long topicId,
            @AuthenticationPrincipal MemberUserDetails userDetails
    ) {
        return memberChatService.openChatRoomFromChatbot(topicId, userDetails.getMemberId());
    }

    // 로그인한 회원 본인의 채팅방 목록 조회
    @GetMapping("/rooms")
    public List<MemberChatRoomListDto> getMemberChatRooms(
            @AuthenticationPrincipal MemberUserDetails userDetails) {
        return memberChatService.getMemberChatRooms(userDetails.getMemberId());
    }

    // 채팅방 상세 조회
    @GetMapping("/rooms/{id}")
    public MemberChatRoomDetailDto getMemberChatRoomDetail (
            @PathVariable("id") Long chRoId,
            @AuthenticationPrincipal MemberUserDetails userDetails) {
        return memberChatService.getMemberChatRoomDetail(chRoId, userDetails.getMemberId());
    }

    // 메시지 전송 처리
    @PostMapping("/rooms/{id}/messages")
    public MemberChatMessageDto sendMemberChatMessage (
            @PathVariable("id") Long chRoId,
            @AuthenticationPrincipal MemberUserDetails userDetails,
            @RequestBody MemberChatMessageSendDto request // 본문 내용의 JSON을 받기 위해
            ) {
        return memberChatService.sendMemberChatMessage(chRoId, userDetails.getMemberId(), request.getChMsCon());
    }

    // 회원이 채팅방 종료 처리
    @PatchMapping("/rooms/{id}/close")
    public void closeMemberChatRoom (
            @PathVariable("id") Long chRoId,
            @AuthenticationPrincipal MemberUserDetails userDetails) {
        memberChatService.closeMemberChatRoom(chRoId, userDetails.getMemberId());
    }
}
