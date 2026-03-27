package com.goodee.beedan.dto.chatbot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 상담사 연결 시 채팅방 제목으로 사용할 최상위 1차 질의 정보를 담는 DTO
public class ChatbotTopLevelTopicDto {
    private Long cbTpId;
    private String cbTpNm;
}
