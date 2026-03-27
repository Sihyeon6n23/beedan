package com.goodee.beedan.dto.member;

import com.goodee.beedan.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PhoneVerificationDto {
    private String name;
    private String phoneNumber;
    private String ci;
}
