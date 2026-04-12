package com.goodee.beedan.service.auth.biz;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goodee.beedan.dto.member.BizDto;
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
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BizValidateService {
    @Qualifier("bizValidationWebClient")
    private final WebClient bizValidationWebClient;
    private final ObjectMapper objectMapper;

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

    public BizDto monoToBizDto(Mono<Map<String, Object>> validateMono) {
        return validateMono.map(responseMap -> {
            // 1. JSON 최상단에서 "data" 리스트를 먼저 꺼냅니다.
            List<Map<String, Object>> dataList = (List<Map<String, Object>>) responseMap.get("data");

            // 2. dataList가 존재하고 비어있지 않은지 체크합니다.
            if (dataList != null && !dataList.isEmpty()) {
                // 3. 배열의 첫 번째 객체를 가져옵니다.
                Map<String, Object> data = dataList.get(0);

                // 4. 객체 안에서 실제 값들을 꺼냅니다.
                String bNo = (String) data.get("b_no");
                String bNm = (String) data.get("b_nm");
                String pNm = (String) data.get("p_nm"); // p_no가 아니라 p_nm(대표자성명)으로 수정
                String startDt = (String) data.get("start_dt");

                String valid = (String) data.get("valid");
                String validMsg = (String) data.get("valid_msg"); // 필요하다면 메시지도 추출

                return BizDto.builder()
                        .bNo(bNo)
                        .bNm(bNm)
                        .pNm(pNm)
                        .startDt(startDt)
                        .valid(valid) // 잊지 말고 넣어주세요!
                        .build();
            }

            // 응답 데이터가 없는 경우 안전하게 빈 DTO를 반환
            return BizDto.builder().build();
        }).block();
    }
}
