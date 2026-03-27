package com.goodee.beedan.service.chatbot;

import com.goodee.beedan.dto.chatbot.ChatbotNextStepDto;
import com.goodee.beedan.dto.chatbot.ChatbotResponseDto;
import com.goodee.beedan.dto.chatbot.ChatbotTopLevelTopicDto;
import com.goodee.beedan.dto.chatbot.ChatbotTopicDto;
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
    private static final int FIRST_LEVEL = 1;
    private static final int SECOND_LEVEL = 2;
    private static final boolean ACTIVE = true;

    private final ChatbotTopicRepository chatbotTopicRepository;
    private final ChatbotResponseRepository chatbotResponseRepository;

    // 챗봇 1차 질의 목록을 조회
    public List<ChatbotTopicDto> getFirstLevelTopics() {
        List<ChatbotTopic> firstLevelTopics = chatbotTopicRepository
                .findByCbTpLvlAndCbTpUseYnOrderByCbTpOrdAsc(FIRST_LEVEL, ACTIVE);

        return firstLevelTopics.stream()
                .map(this::mapToChatbotTopicDto)
                .toList();
    }

    // 다음 화면이 하위(2차) 질의 목록인지 최종 응답인지 판단 후 조회
    // (반환값이 항상 같은 타입이 아니기 때문에 wrapperDto 인 ChatbotNextStepDto 사용)
    public ChatbotNextStepDto getNextStep(Long topicId) {
        ChatbotTopic topic = chatbotTopicRepository
                .findByCbTpIdAndCbTpUseYn(topicId, ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 비활성화된 챗봇 질의입니다."));

        // 현재 질의 아래에 하위(2차) 질의가 있는지
        boolean hasChildTopics = chatbotTopicRepository
                .existsByCbTpPrnIdAndCbTpLvlAndCbTpUseYn(topic.getCbTpId(), SECOND_LEVEL, ACTIVE);

        if (hasChildTopics) { // 하위 질의가 있다면
            // 다음 화면에 보여줄 버튼 목록 조회
            List<ChatbotTopic> childTopics = chatbotTopicRepository
                    .findByCbTpPrnIdAndCbTpLvlAndCbTpUseYnOrderByCbTpOrdAsc(topic.getCbTpId(), SECOND_LEVEL, ACTIVE);

            List<ChatbotTopicDto> childTopicDtoList = childTopics.stream()
                    .map(this::mapToChatbotTopicDto)
                    .toList();

            return createTopicStepDto(childTopicDtoList);
        }

        // 하위 질의가 없다면 현재 질의의 최종 응답 조회
        ChatbotResponse chatbotResponse = chatbotResponseRepository
                .findByCbTpIdAndCbResUseYn(topic.getCbTpId(), ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("연결된 챗봇 응답이 없습니다."));

        ChatbotResponseDto chatbotResponseDto = mapToChatbotResponseDto(chatbotResponse);

        return createResponseStepDto(chatbotResponseDto);
    }

    // 채팅방 제목에 사용할 최상위 1차 질의 조회
    public ChatbotTopLevelTopicDto getTopLevelTopic(Long topicId) {
        ChatbotTopic topic = chatbotTopicRepository
                .findByCbTpIdAndCbTpUseYn(topicId, ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 비활성화된 챗봇 질의입니다."));

        // 현재 질의가 1차 질의면
        if (topic.getCbTpLvl().equals(FIRST_LEVEL)) {
            return mapToChatbotTopLevelTopicDto(topic); // 바로 반환
        }

        // 2차 질의면 부모 1차 질의를 다시 조회
        ChatbotTopic parentTopic = chatbotTopicRepository
                .findByCbTpIdAndCbTpUseYn(topic.getCbTpPrnId(), ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("상위 1차 질의를 찾을 수 없습니다."));

        return mapToChatbotTopLevelTopicDto(parentTopic);
    }

    // ChatbotTopic -> ChatbotTopicDto
    private ChatbotTopicDto mapToChatbotTopicDto(ChatbotTopic chatbotTopic) {
        return ChatbotTopicDto.builder()
                .cbTpId(chatbotTopic.getCbTpId())
                .cbTpNm(chatbotTopic.getCbTpNm())
                .cbTpLvl(chatbotTopic.getCbTpLvl())
                .cbTpPrnId(chatbotTopic.getCbTpPrnId())
                .cbTpOrd(chatbotTopic.getCbTpOrd())
                .build();
    }

    // ChatbotResponse -> ChatbotResponseDto
    private ChatbotResponseDto mapToChatbotResponseDto(ChatbotResponse chatbotResponse) {
        return ChatbotResponseDto.builder()
                .cbResId(chatbotResponse.getCbResId())
                .cbResTtl(chatbotResponse.getCbResTtl())
                .cbResCon(chatbotResponse.getCbResCon())
                .cbResLnkBtnNm(chatbotResponse.getCbResLnkBtnNm())
                .cbResLnkUrl(chatbotResponse.getCbResLnkUrl())
                .cbTpId(chatbotResponse.getCbTpId())
                .build();
    }

    // ChatbotTopLevelTopic -> ChatbotTopLevelTopicDto
    private ChatbotTopLevelTopicDto mapToChatbotTopLevelTopicDto(ChatbotTopic chatbotTopic) {
        return ChatbotTopLevelTopicDto.builder()
                .cbTpId(chatbotTopic.getCbTpId())
                .cbTpNm(chatbotTopic.getCbTpNm())
                .build();
    }

    // 하위(2차) 질의 목록일 때 반환할 DTO 생성
    private ChatbotNextStepDto createTopicStepDto(List<ChatbotTopicDto> topics) {
        return ChatbotNextStepDto.builder()
                .stepType("TOPIC")
                .topics(topics)
                .response(null)
                .build();
    }

    // 최종 응답일 때 반환할 DTO 생성
    private ChatbotNextStepDto createResponseStepDto(ChatbotResponseDto response) {
        return ChatbotNextStepDto.builder()
                .stepType("RESPONSE")
                .topics(null)
                .response(response)
                .build();
    }
}
