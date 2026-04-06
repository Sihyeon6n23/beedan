package com.goodee.beedan.service.member;

import com.goodee.beedan.dto.member.sns.SnsDisconnectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service("KAKAO")
@RequiredArgsConstructor
@Slf4j
public class KakaoUnlinkService implements SnsUnlinkServices {
    @Qualifier("kakaoApiClient")
    private final WebClient webClient;

    @Value("${sns.kakao.admin.key}")
    private String adminKey;

    @Override
    public Mono<String> unlink(SnsDisconnectRequest snsDisconnectRequest) {
        return webClient.post()
                .uri("/v1/user/unlink")
                .header(HttpHeaders.AUTHORIZATION,"KakaoAK " + adminKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=utf-8")
                .body(BodyInserters.fromFormData("target_id_type","user_id")
                        .with("target_id", snsDisconnectRequest.getSnsSeNo()))
                .retrieve()
                .bodyToMono(String.class);
    }
}
