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
// 회원 채팅방 상세 화면에서 메시지 한 건을 표현하기 위한 DTO
public class MemberChatMessageDto {
    private Long chMsId;
    private ChatMessageSenderType chMsSenTy;
    private ChatMessageType chMsTp;
    private String chMsCon;
    private String chMsLnkUrl;
    private String chMsLnkTtl;
    private LocalDateTime chMsCreDt;

    private FileDto imageFile;
}