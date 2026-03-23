package com.goodee.beedan.dto.exchangeRate;

import lombok.Data;

import java.util.Map;

@Data
public class ExchangeRateResponseDto {
    private String result;     // success 또는 error
    private String base_code;  // 기준 통화 (예: KRW)
    private Map<String, Double> rates; // 각 국가별 환율 데이터

}
