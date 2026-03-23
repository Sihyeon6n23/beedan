package com.goodee.beedan.client.exchangeRate;

import com.goodee.beedan.dto.exchangeRate.ExchangeRateResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ExchangeRateClient {

    private final WebClient webclient;

    @Value("${exchange.api.key}")
    private String apiKey;

    @Value("${exchange.api.url}")
    private String apiUrl;

    public Map<String, Double> fetchKrwRates() {

        ExchangeRateResponseDto response = webclient.get()
                .uri(apiUrl + "/" + apiKey + "/latest/KRW")
                .retrieve()
                .bodyToMono(ExchangeRateResponseDto.class)
                .block();

        if (response == null || !"success".equals(response.getResult())) {
            throw new RuntimeException("환율 API 호출 실패");
        }

        List<String> targets = List.of("JPY", "USD", "EUR", "CNY");
        return response.getRates().entrySet().stream()
                .filter(e->targets.contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    }


}
