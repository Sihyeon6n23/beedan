package com.goodee.beedan.dto.member.sns;

import com.goodee.beedan.common.constant.SnsType;
import com.goodee.beedan.entity.Member;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SnsIntegrateResponse {
    // 타입, 연결시기, 연결여부
    private SnsType snsTp;
    private LocalDateTime snsConDt;
    private boolean connected;
}
