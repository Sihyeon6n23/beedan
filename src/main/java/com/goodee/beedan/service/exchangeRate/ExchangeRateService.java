package com.goodee.beedan.service.exchangeRate;

import com.goodee.beedan.client.exchangeRate.ExchangeRateClient;
import com.goodee.beedan.repository.exchangeRate.ExchangeRateRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ExchangeRateService {

    private final ExchangeRateRepository repository;
    private final ExchangeRateClient client;

}
