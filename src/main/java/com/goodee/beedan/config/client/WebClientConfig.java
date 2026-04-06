package com.goodee.beedan.config.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @Primary
    public WebClient webClient() {
        return WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Value("${portone.phone.certification.api.url}")
    private String portOneUrl;
    @Bean("portOneWebClient")
    public WebClient portOneWebClient() {
        return WebClient.builder()
                .baseUrl(portOneUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Value("${biz.validation.api.url}")
    private String bizValidationUrl;
    @Bean("bizValidationWebClient")
    public WebClient bizValidationWebClient() {
        return WebClient.builder()
                .baseUrl(bizValidationUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public WebClient kakaoAuthClient() {
        return WebClient.builder()
                .baseUrl("https://kauth.kakao.com") // 토큰 발급용 도메인
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
    }

    @Bean
    public WebClient kakaoApiClient() {
        return WebClient.builder()
                .baseUrl("https://kapi.kakao.com") // 사용자 정보 및 기능 API 도메인
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
    }

}
