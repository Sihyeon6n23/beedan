package com.goodee.beedan.service.auth.kakao;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Data
@Slf4j
public class KakaoService {
    @Qualifier("kakaoAuthClient")
    private final WebClient authClient;
    @Qualifier("kakaoApiClient")
    private final WebClient apiClient;

    @Value("${sns.kakao.client.api.key}")
    private String clientId;
    @Value("${sns.kakao.client.secret.key}")
    private String clientSecret;

    @Value("${sns.kakao.redirect.url}")
    private String redirectUrl;

    // 웹클라이언트에서 바로 String 으로 맵핑
    public String getAccessToken(String code) {
        return authClient.post()
                .uri("/oauth/token")
                .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                        .with("client_id", clientId)
                        .with("redirect_uri", redirectUrl)
                        .with("code", code)
                        .with("client_secret", clientSecret))
                .retrieve()
                // Map<String, String>으로 받기
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {})
                .map(map -> map.get("access_token")).block();
    }

    public Long getKakaoId(String accessToken) {
        return apiClient.get()
                .uri("/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(response -> {
                    // 카카오 고유 번호 추출 (Long 타입)
                    Long kakaoId = Long.valueOf(String.valueOf(response.get("id")));
                    log.info("조회된 카카오 고유번호: {}", kakaoId);
                    return kakaoId;
                }).block();
    }
}
