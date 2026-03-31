package com.goodee.beedan.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotResponseDto { // 최종 응답용 Dto
    private Long cbResId;
    private String cbResTtl;
    private String cbResCon;
    private String cbResLnkBtnNm;
    private String cbResLnkUrl;

    private Long cbTpId;
}
