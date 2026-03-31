package com.goodee.beedan.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 관리자 채팅방 상세 화면에서 전송할 메시지 본문을 담는 요청 DTO
public class AdminChatMessageSendDto {
    private String chMsCon;
}
