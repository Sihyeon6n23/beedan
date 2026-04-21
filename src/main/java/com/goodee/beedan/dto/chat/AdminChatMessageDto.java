package com.goodee.beedan.dto.chat;

import com.goodee.beedan.common.constant.ChatMessageSenderType;
import com.goodee.beedan.common.constant.ChatMessageType;
import com.goodee.beedan.dto.file.FileDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
// 관리자 상세 화면에서 메시지 1건 표현
public class AdminChatMessageDto {
    private Long chMsId;
    private ChatMessageSenderType chMsSenTy;
    private ChatMessageType chMsTp;
    private String chMsCon;
    private String chMsLnkUrl;
    private String chMsLnkTtl;
    private LocalDateTime chMsCreDt;

    private FileDto imageFile;
}
