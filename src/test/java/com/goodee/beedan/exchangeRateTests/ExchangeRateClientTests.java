package com.goodee.beedan.exchangeRateTests;

import com.goodee.beedan.client.exchangeRate.ExchangeRateClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

@SpringBootTest
public class ExchangeRateClientTests {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateClientTests.class);
    @Autowired
    private ExchangeRateClient exchangeRateClient;

    @Test
    @DisplayName("외부 환율 API 호출 시 JPY/USD/EUR/CNY 데이터가 정상적으로 오는지 확인")
    void fetchKrwRates() {

        Map<String, Double> rates = exchangeRateClient.fetchKrwRates();

        assertThat(rates).isNotNull();
        assertThat(rates).containsKeys("JPY", "USD", "EUR", "CNY");
        assertThat(rates.get("JPY")).isGreaterThan(0);
        assertThat(rates.get("USD")).isGreaterThan(0);

        log.info("=== 환율 ===");
        rates.forEach((currency, rate) ->
                log.info(currency + " : " + rate));
    }


}
