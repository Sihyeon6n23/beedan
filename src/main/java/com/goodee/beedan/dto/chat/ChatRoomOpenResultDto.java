package com.goodee.beedan.dto.chat;

import com.goodee.beedan.common.constant.ChatRoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 챗봇 또는 사용자 액션으로 채팅방을 열 때 새 방 생성 여부와 이동 대상 방 정보를 반환하는 DTO
public class ChatRoomOpenResultDto {
    private Long chRoId;
    private String chRoTtl;
    private ChatRoomStatus chRoStt;
    private Boolean existingRoom;
}
