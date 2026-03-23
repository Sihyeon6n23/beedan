package com.goodee.beedan.scheduler.exchangeRate;

import com.goodee.beedan.client.exchangeRate.ExchangeRateClient;
import com.goodee.beedan.repository.exchangeRate.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeRateScheduler {
    private final ExchangeRateClient client;
    private final ExchangeRateRepository repository;
}
