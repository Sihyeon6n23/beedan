package com.goodee.beedan.service.auth.biz;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goodee.beedan.dto.member.biz.BizDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BizValidateService {
    @Qualifier("bizValidationWebClient")
    private final WebClient bizValidationWebClient;
    private ObjectMapper objectMapper;

    @Value("${biz.validation.api.key}")
    private String apiKey;

    public Mono<Map<String, Object>> validate(BizDto bizDto) {
        Map<String, Object> bizDtoMap = objectMapper.convertValue(bizDto, Map.class);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("businesses", Collections.singletonList(bizDtoMap));

        Mono<Map<String, Object>> mono = bizValidationWebClient.post()
                .uri(uriBuilder -> uriBuilder
                            .queryParam("serviceKey", apiKey)
                            .build()
                )
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>(){});
        return mono;
    }
}
