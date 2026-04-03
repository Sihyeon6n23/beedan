package com.goodee.beedan.dto.member.sns;

import com.goodee.beedan.common.constant.SnsType;
import com.goodee.beedan.entity.Member;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SnsIntegrateRequest {
    // SNS 타입과 시리얼번호, 어떤 멤버가 연결하려고 한건지
    private SnsType snsTp;
    private String snsSeNo;
    private Member member;
}
