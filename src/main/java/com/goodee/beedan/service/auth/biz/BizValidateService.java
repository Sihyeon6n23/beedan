package com.goodee.beedan.service.auth.biz;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goodee.beedan.dto.biz.BizDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BizValidateService {
    private final WebClient webClient;

    @Value("${biz.validation.api.key}")
    private String apiKey;

    @Value("${biz.validation.api.uri}")
    private String apiUri;

    public Mono<Map<String, Object>> validate(BizDto bizDto) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> bizDtoMap = objectMapper.convertValue(bizDto, Map.class);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("businesses", Collections.singletonList(bizDtoMap));

        Mono<Map<String, Object>> mono = webClient.post()
                .uri(uriBuilder -> {
                    return UriComponentsBuilder.fromUriString(apiUri)
                            .queryParam("serviceKey", apiKey)
                            .build()
                            .toUri();
                })
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>(){});
        return mono;
    }
}
