package com.goodee.beedan.dto.chatbot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 챗봇의 1차/2차 질의 버튼 목록을 화면에 보여주기 위한 DTO
public class ChatbotTopicDto {
    private Long cbTpId;
    private String cbTpNm;
    private Integer cbTpLvl;
    private Long cbTpPrnId;
    private Integer cbTpOrd;
}
