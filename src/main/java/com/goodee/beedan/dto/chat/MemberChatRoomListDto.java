package com.goodee.beedan.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 회원 위젯의 채팅방 목록 화면에서 채팅방 기본 정보와 마지막 메시지 정보를 보여주기 위한 DTO
public class MemberChatRoomListDto {
    private Long chRoId;
    private String chRoTtl;
    private String chRoStt;
    private String lastMessageContent;
    private LocalDateTime lastMessageCreatedAt;
    private LocalDateTime chRoCreDt;
    private Boolean unread;
}
