package com.goodee.beedan.service.quote;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class QuoteNameService {

    @Value("${gemini.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * AI로 협상 이름 생성
     * @param companyName 고객사명
     * @param productNames 상품명 목록
     * @return 예: "(주)비단물산 에센셜티셔츠외3건"
     */
    public String generateName(String companyName, List<String> productNames) {
        try {
            String prompt = "고객사명은 '" + companyName + "'이고 상품은 " + productNames + "입니다. " +
                    "20자 이내로 '고객사명 대표상품외N건' 형식의 견적 이름을 만들어주세요. " +
                    "언더바(_)는 사용하지 말고 공백으로 구분하세요. " +
                    "대표상품은 첫 번째 상품명을 짧게 줄여주세요. " +
                    "상품이 1개면 '외N건'은 빼주세요. " +
                    "반드시 이름만 응답하세요. 따옴표나 설명 없이 이름만.";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                    "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            String name = root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText().trim();

            // 따옴표, 언더바 제거
            name = name.replace("\"", "").replace("'", "").replace("_", " ").trim();

            log.info("AI 견적 이름 생성: {}", name);
            return name;

        } catch (Exception e) {
            log.error("AI 견적 이름 생성 실패: {}", e.getMessage());
            // 실패 시 기본 이름
            String fallback = companyName + " " + productNames.get(0);
            if (productNames.size() > 1) {
                fallback += "외" + (productNames.size() - 1);
            }
            return fallback.length() > 20 ? fallback.substring(0, 20) : fallback;
        }
    }
}
