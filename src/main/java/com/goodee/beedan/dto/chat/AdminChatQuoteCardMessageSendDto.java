package com.goodee.beedan.dto.chat;

import lombok.Data;

@Data
// 관리자 견적(내용 + 링크 + 견적 카드 제목) 전송용 Dto
public class AdminChatQuoteCardMessageSendDto {
    private String chMsCon;     // 카드 설명 문구, 내용(선택)
    private String chMsLnkTtl;  // 카드 제목
    private String chMsLnkUrl;  // 견적 링크
}
