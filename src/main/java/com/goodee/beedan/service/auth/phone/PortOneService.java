package com.goodee.beedan.service.auth.phone;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goodee.beedan.dto.member.PhoneVerificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortOneService {
    @Qualifier("portOneWebClient")
    private final WebClient portOneWebClient;
    private final ObjectMapper objectMapper;
    @Value("${portone.phone.certification.api.key}")
    private String apiKey;

    public Mono<Map<String, Object>> verify(String impUid) {
        log.info("====> [PortOne API 호출] impUid: {}", impUid);

        return portOneWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/identity-verifications/{id}")
                        .build(impUid)
                )
                .header("Authorization", "PortOne " + apiKey)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>(){})
                .doOnNext(response -> {
                    log.info("====> [PortOne API 응답 수신 완료]");
                });
    }

    public PhoneVerificationDto MonoToPhoneVerificationDto(Mono<Map<String, Object>> verifyMono) {
        return verifyMono.map(map -> {
            // 1. 'verifiedCustomer'라는 내부 Map(상자)을 먼저 꺼냅니다.
            @SuppressWarnings("unchecked")
            Map<String, Object> customer = (Map<String, Object>) map.get("verifiedCustomer");

            String name = null;
            String phoneNumber = null;
            String ci = null;

            if (customer != null) {
                // 2. 이제 상자 안(customer)에서 값을 꺼냅니다.
                name = (String) customer.get("name");
                phoneNumber = (String) customer.get("phoneNumber");
                ci = (String) customer.get("ci");
            } else {
                // 혹시라도 예전 버전(평면 구조)으로 데이터가 올 경우를 대비한 하위 호환
                name = (String) map.get("name");
                phoneNumber = (String) map.get("phoneNumber");
                ci = (String) map.get("ci");
            }

            log.info("====> [진짜 추출 성공!] name: {}, phoneNumber: {}, ci: {}", name, phoneNumber, ci);

            return PhoneVerificationDto.builder()
                    .name(name)
                    .phoneNumber(phoneNumber)
                    .ci(ci)
                    .build();
        }).block();
    }
}