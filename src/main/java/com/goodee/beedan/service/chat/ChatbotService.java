package com.goodee.beedan.service.chat;

import com.goodee.beedan.dto.chat.ChatbotNextStepDto;
import com.goodee.beedan.dto.chat.ChatbotResponseDto;
import com.goodee.beedan.dto.chat.ChatbotTopLevelTopicDto;
import com.goodee.beedan.dto.chat.ChatbotTopicDto;
import com.goodee.beedan.entity.ChatbotResponse;
import com.goodee.beedan.entity.ChatbotTopic;
import com.goodee.beedan.repository.chatbot.ChatbotResponseRepository;
import com.goodee.beedan.repository.chatbot.ChatbotTopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatbotService {
    private final ChatbotTopicRepository chatbotTopicRepository;
    private final ChatbotResponseRepository chatbotResponseRepository;

    public List<ChatbotTopicDto> getFirstLevelTopics() {
        return chatbotTopicRepository.findByCbTpLvlAndCbTpUseYnOrderByCbTpOrdAsc(1, true)
                .stream().map(this::mapToChatbotTopicDto).toList();
    }

    public ChatbotTopicDto mapToChatbotTopicDto(ChatbotTopic chatbotTopic) {
        return ChatbotTopicDto.builder()
                .cbTpId(chatbotTopic.getCbTpId())
                .cbTpNm(chatbotTopic.getCbTpNm())
                .cbTpLvl(chatbotTopic.getCbTpLvl())
                .cbTpPrnId(chatbotTopic.getCbTpPrnId())
                .cbTpOrd(chatbotTopic.getCbTpOrd())
                .build();
    }

    public ChatbotResponseDto mapToChatbotResponseDto(ChatbotResponse chatbotResponse) {
        return ChatbotResponseDto.builder()
                .cbResId(chatbotResponse.getCbResId())
                .cbResTtl(chatbotResponse.getCbResTtl())
                .cbResCon(chatbotResponse.getCbResCon())
                .cbResLnkBtnNm(chatbotResponse.getCbResLnkBtnNm())
                .cbResLnkUrl(chatbotResponse.getCbResLnkUrl())
                .cbTpId(chatbotResponse.getCbTpId())
                .build();
    }

    public ChatbotTopLevelTopicDto mapToChatbotTopLevelTopicDto(ChatbotTopic chatbotTopic) {
        return ChatbotTopLevelTopicDto.builder()
                .cbTpId(chatbotTopic.getCbTpId())
                .cbTpNm(chatbotTopic.getCbTpNm())
                .build();
    }

    public ChatbotNextStepDto createTopicStepDto(List<ChatbotTopicDto> topics) {
        return ChatbotNextStepDto.builder()
                .stepType("TOPIC")
                .topics(topics)
                .response(null)
                .build();
    }

    public ChatbotNextStepDto createResponseStepDto(ChatbotResponseDto response) {
        return ChatbotNextStepDto.builder()
                .stepType("RESPONSE")
                .topics(null)
                .response(response)
                .build();
    }

}
