package com.goodee.beedan.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotTopicDto { // 버튼용 Dto
    private Long cbTpId;
    private String cbTpNm;
    private Integer cbTpLvl;

    private Long cbTpPrnId;
    private Integer cbTpOrd;
}
