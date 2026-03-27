package com.goodee.beedan.dto.quote;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 견적 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuoteBaseRequest {

    @NotNull(message = "협상 아이디는 필수입니다")
    private Long ngId;

    @NotNull(message = "송신자 아이디는 필수입니다")
    private Long quSid;

    @NotNull(message = "수신자 아이디는 필수입니다")
    private Long quRid;
}