package com.goodee.beedan.dto.quote;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 해외 운임표 수정 요청 DTO
 * 운임비만 수정 가능 (국가코드, 운송수단은 변경 불가)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingRateUpdateRequest {

    @NotNull(message = "소형 운임비는 필수입니다")
    private BigDecimal srSmAm;

    @NotNull(message = "중형 운임비는 필수입니다")
    private BigDecimal srMdAm;

    @NotNull(message = "대형 운임비는 필수입니다")
    private BigDecimal srLgAm;

    private String srDes;
}