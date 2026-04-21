package com.goodee.beedan.dto.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PhoneVerificationDto {
    private String name;
    private String phoneNumber;
    private String ci;
}
