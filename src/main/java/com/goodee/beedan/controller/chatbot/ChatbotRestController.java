package com.goodee.beedan.controller.chatbot;

import com.goodee.beedan.dto.chatbot.ChatbotNextStepDto;
import com.goodee.beedan.dto.chatbot.ChatbotTopLevelTopicDto;
import com.goodee.beedan.dto.chatbot.ChatbotTopicDto;
import com.goodee.beedan.service.chatbot.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatbot")
public class ChatbotRestController {
    private final ChatbotService chatbotService;

    // 1차 질의 목록 조회
    @GetMapping("/topics/first")
    public List<ChatbotTopicDto> getFirstLevelTopics() {
        return chatbotService.getFirstLevelTopics();
    }

    // 다음 단계 조회 (2차 질의 또는 최종 응답)
    @GetMapping("/topics/{topicId}/next")
    public ChatbotNextStepDto getNextStep(@PathVariable Long topicId) {
        return chatbotService.getNextStep(topicId);
    }

    // 최상위 1차 질의 조회 (채팅방 제목용)
    @GetMapping("/topics/{topicId}/top-level")
    public ChatbotTopLevelTopicDto getTopLevelTopic(@PathVariable Long topicId) {
        return chatbotService.getTopLevelTopic(topicId);
    }
}
