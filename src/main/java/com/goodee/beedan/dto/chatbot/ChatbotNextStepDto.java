package com.goodee.beedan.dto.chatbot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 사용자가 질의를 선택했을 때 다음 화면이 버튼 목록인지 최종 응답인지 함께 전달하는 Wrapper DTO
public class ChatbotNextStepDto {
    // TOPIC 이면 topics를, RESPONSE 이면 response를 사용
    private String stepType;
    private List<ChatbotTopicDto> topics;
    private ChatbotResponseDto response;
}
