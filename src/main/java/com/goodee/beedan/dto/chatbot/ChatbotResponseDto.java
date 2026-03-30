package com.goodee.beedan.dto.chatbot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 챗봇의 최종 응답 화면에서 제목, 본문, 링크 정보를 보여주기 위한 DTO
public class ChatbotResponseDto {
    private Long cbResId;
    private String cbResTtl;
    private String cbResCon;
    private String cbResLnkBtnNm;
    private String cbResLnkUrl;
    private Long cbTpId;
}
