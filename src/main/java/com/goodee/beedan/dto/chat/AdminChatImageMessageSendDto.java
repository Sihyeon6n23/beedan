package com.goodee.beedan.dto.chat;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
// 관리자 이미지 메시지 전송용 Dto
public class AdminChatImageMessageSendDto {
    private MultipartFile imageFile;    // 단일 이미지
}
