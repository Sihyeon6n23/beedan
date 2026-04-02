package com.goodee.beedan.client.news;

import com.goodee.beedan.dto.news.NewsResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class NewsClient {

    @Qualifier("webClient")
    private final WebClient webClient;

    @Value("${naver.api.client-id}")
    private String clientId;

    @Value("${naver.api.client-secret}")
    private String clientSecret;

    public NewsResponseDto searchNews(String query) {
        return webClient.get()
                .uri("https://openapi.naver.com/v1/search/news.json?query={q}&display=4&sort=date",
                        query)
                .header("X-Naver-Client-Id", clientId)
                .header("X-Naver-Client-Secret", clientSecret)
                .retrieve()
                .bodyToMono(NewsResponseDto.class)
                .block();
    }
}
