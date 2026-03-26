package com.goodee.beedan.dto.buyer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeePolicyUpdateRequest {

    @NotBlank(message = "계산 방식은 필수입니다")
    private String fpCalcTy;

    @NotNull(message = "계산 값은 필수입니다")
    private BigDecimal fpVal;

    @NotNull(message = "적용 시작일은 필수입니다")
    private LocalDateTime fpEfFrDt;

    private LocalDateTime fpEfToDt;

    private String fpDes;
}