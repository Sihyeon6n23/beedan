package com.goodee.beedan.dto.member.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignInErrorMessageDto {
    private String errorType;
    private String message;
}
