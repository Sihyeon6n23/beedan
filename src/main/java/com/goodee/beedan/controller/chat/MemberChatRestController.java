package com.goodee.beedan.controller.chat;

import com.goodee.beedan.config.security.MemberUserDetails;
import com.goodee.beedan.dto.chat.ChatRoomOpenResultDto;
import com.goodee.beedan.dto.chat.MemberChatMessageDto;
import com.goodee.beedan.dto.chat.MemberChatMessageSendDto;
import com.goodee.beedan.dto.chat.MemberChatRoomDetailDto;
import com.goodee.beedan.dto.chat.MemberChatRoomListDto;
import com.goodee.beedan.service.chat.MemberChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class MemberChatRestController {
    private final MemberChatService memberChatService;

    // 회원이 + 버튼으로 새 문의하기 시 기타 문의 채팅방 생성 또는 기존 활성방 반환
    @PostMapping("/rooms")
    public ChatRoomOpenResultDto openNewInquiryChatRoom(
            @AuthenticationPrincipal MemberUserDetails userDetails) {
        return memberChatService.openNewInquiryChatRoom(userDetails.getMemberId());
    }

    // 챗봇 최종 응답에서 상담 연결 시 기존 활성방 반환 또는 새 OPEN 채팅방 생성
    @PostMapping("/rooms/from-chatbot/{topicId}")
    public ChatRoomOpenResultDto openChatRoomFromChatbot(
            @PathVariable Long topicId,
            @AuthenticationPrincipal MemberUserDetails userDetails) {
        return memberChatService.openChatRoomFromChatbot(topicId, userDetails.getMemberId());
    }

    // 로그인한 회원 본인의 채팅방 목록 조회
    @GetMapping("/rooms")
    public List<MemberChatRoomListDto> getMemberChatRooms(
            @AuthenticationPrincipal MemberUserDetails userDetails) {
        return memberChatService.getMemberChatRooms(userDetails.getMemberId());
    }

    // 회원 본인 채팅방 상세 조회
    @GetMapping("/rooms/{id}")
    public MemberChatRoomDetailDto getMemberChatRoomDetail(
            @PathVariable("id") Long chRoId,
            @AuthenticationPrincipal MemberUserDetails userDetails) {
        return memberChatService.getMemberChatRoomDetail(chRoId, userDetails.getMemberId());
    }

    // 회원 본인 채팅방에 메시지 전송
    @PostMapping("/rooms/{id}/messages")
    public MemberChatMessageDto sendMemberChatMessage(
            @PathVariable("id") Long chRoId,
            @AuthenticationPrincipal MemberUserDetails userDetails,
            @RequestBody MemberChatMessageSendDto request // 요청 본문(JSON)
    ) {
        return memberChatService.sendMemberChatMessage(chRoId, userDetails.getMemberId(), request.getChMsCon());
    }

    // 회원이 본인 채팅방을 종료 처리
    @PatchMapping("/rooms/{id}/close")
    public void closeMemberChatRoom(
            @PathVariable("id") Long chRoId,
            @AuthenticationPrincipal MemberUserDetails userDetails) {
        memberChatService.closeMemberChatRoom(chRoId, userDetails.getMemberId());
    }
}