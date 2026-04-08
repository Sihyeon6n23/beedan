package com.goodee.beedan.dto.chat;

import com.goodee.beedan.common.constant.ChatRoomCloseReason;
import com.goodee.beedan.common.constant.ChatRoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 회원 채팅방 상세 화면에서 채팅방 정보와 메시지 목록을 함께 전달하기 위한 DTO
public class MemberChatRoomDetailDto {
    private Long chRoId;
    private String chRoTtl;
    private ChatRoomStatus chRoStt;
    private List<MemberChatMessageDto> messages;

    private ChatRoomCloseReason chRoClsRsn;
}