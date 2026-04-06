package com.goodee.beedan.dto.member.sns;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class MessageResponse {
    private String message;
    private String redirectPath;
}
