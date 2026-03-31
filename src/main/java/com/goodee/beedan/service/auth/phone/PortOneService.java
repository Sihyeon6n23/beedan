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
        Mono<Map<String, Object>> mono = portOneWebClient.get()
                .uri(uriBuilder -> uriBuilder
                            .path("/identity-verifications/{id}")
                            .build(impUid)
                )
                .header("Authorization", "PortOne " + apiKey)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>(){});
        return mono;
    }

    public PhoneVerificationDto MonoToPhoneVerificationDto(Mono<Map<String, Object>> verifyMono) {
        return verifyMono.map(map -> {
            String name = (String) map.get("name");
            String phoneNumber = (String) map.get("phoneNumber");
            String ci = (String) map.get("ci");
            return PhoneVerificationDto.builder()
                    .name(name)
                    .phoneNumber(phoneNumber)
                    .ci(ci)
                    .build();
        }).block();
    }
}
