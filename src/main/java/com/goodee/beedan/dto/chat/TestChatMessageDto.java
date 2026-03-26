package com.goodee.beedan.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TestChatMessageDto {
    private Long roomId;
    private String sender;
    private String content;
}
