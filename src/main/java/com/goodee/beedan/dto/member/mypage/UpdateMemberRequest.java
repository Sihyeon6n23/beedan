package com.goodee.beedan.dto.member.mypage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class UpdateMemberRequest {
    private String name;
    private String phone;
    private String email;
    private String impUid;
    private String ci;
}