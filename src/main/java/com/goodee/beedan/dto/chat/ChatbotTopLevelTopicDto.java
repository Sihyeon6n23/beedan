package com.goodee.beedan.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotTopLevelTopicDto { // 상담사 연결 시 채팅방 제목용
    private Long cbTpId;
    private String cbTpNm;
}
