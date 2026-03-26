package com.goodee.beedan.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotNextStepDto { // 다음이 버튼 목록인지 응답인지
    private String stepType; // TOPIC 또는 RESPONSE
    private List<ChatbotTopicDto> topics;
    private ChatbotResponseDto response;
}
