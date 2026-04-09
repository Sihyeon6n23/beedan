package com.goodee.beedan.dto.chat;

import com.goodee.beedan.common.constant.ChatRoomCloseReason;
import com.goodee.beedan.common.constant.ChatRoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 종료 사유를 채팅방 구독자에게 전송하기 위한 Dto
public class ChatRoomStatusEventDto {
    private String eventType;
    private Long chRoId;
    private ChatRoomStatus chRoStt;
    private ChatRoomCloseReason chRoClsRsn;
}
