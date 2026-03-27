package com.goodee.beedan.dto.exchangeRate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 환율 등록 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRateRequest {

    @NotBlank(message = "통화 코드는 필수입니다")
    private String erCr;                // JPY | USD | EUR | CNY

    @NotNull(message = "환율은 필수입니다")
    @DecimalMin(value = "0.0", inclusive = false, message = "환율은 0보다 커야 합니다")
    private BigDecimal erRa;            // 환율

    @NotNull(message = "환율 기준 시간은 필수입니다")
    private LocalDateTime erFDt;        // 환율 기준 시간
}