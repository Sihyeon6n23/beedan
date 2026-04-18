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
    private String memMbPhn;
    private String memEml;
    private String impUid;
    private String memCi;

    // 주소 관련 필드 추가
    private String postCode;       // 우편번호 (memPosCd)
    private String address;        // 기본주소 (memBizAdr)
    private String addressDetail;  // 상세주소 (memBizDtAdr)
}