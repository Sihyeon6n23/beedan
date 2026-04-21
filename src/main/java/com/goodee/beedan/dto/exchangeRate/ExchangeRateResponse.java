package com.goodee.beedan.dto.exchangeRate;

import com.goodee.beedan.entity.ExchangeRate;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 환율 응답 DTO
 */
@Getter
@Builder
public class ExchangeRateResponse {

    private Long erId;
    private String erCr;
    private BigDecimal erRa;
    private String erBa;
    private LocalDateTime erFDt;
    private LocalDateTime erCrDt;

    public static ExchangeRateResponse from(ExchangeRate exchangeRate) {
        return ExchangeRateResponse.builder()
                .erId(exchangeRate.getErId())
                .erCr(exchangeRate.getErCr())
                .erRa(exchangeRate.getErRa())
                .erBa(exchangeRate.getErBa())
                .erFDt(exchangeRate.getErFDt())
                .erCrDt(exchangeRate.getErCrDt())
                .build();
    }
}