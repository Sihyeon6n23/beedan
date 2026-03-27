package com.goodee.beedan.dto.quote;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 항만비/통관수수료 수정 요청 DTO
 * 금액만 수정 가능 (비용 종류 변경 불가)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortCustomsRateUpdateRequest {

    @NotNull(message = "소형 금액은 필수입니다")
    private BigDecimal pcrSmAm;

    @NotNull(message = "중형 금액은 필수입니다")
    private BigDecimal pcrMdAm;

    @NotNull(message = "대형 금액은 필수입니다")
    private BigDecimal pcrLgAm;

    private String pcrDes;
}