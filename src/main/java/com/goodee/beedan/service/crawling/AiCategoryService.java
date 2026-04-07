package com.goodee.beedan.service.crawling;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AiCategoryService {

    @Value("${gemini.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 상품명 목록을 카테고리 목록 중 하나로 자동 분류
     * @return Map<상품명, 카테고리명>
     */
    public Map<String, String> categorize(List<String> productNames, List<String> categoryNames) {
        String prompt = buildPrompt(productNames, categoryNames);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        return parseResponse(response.getBody(), productNames, categoryNames);
    }

    private String buildPrompt(List<String> productNames, List<String> categoryNames) {
        StringBuilder sb = new StringBuilder();
        sb.append("카테고리 목록: ").append(String.join(", ", categoryNames)).append("\n\n");
        sb.append("아래 상품들을 위 카테고리 중 가장 적합한 카테고리 하나로 분류하세요.\n");
        sb.append("반드시 JSON 형식으로만 응답하세요 (다른 텍스트 없이):\n");
        sb.append("{\"상품명\": \"카테고리명\", ...}\n\n");
        sb.append("상품 목록:\n");
        productNames.forEach(name -> sb.append("- ").append(name).append("\n"));
        return sb.toString();
    }

    private Map<String, String> parseResponse(String responseBody, List<String> productNames, List<String> fallbackCategory) {
        Map<String, String> result = new HashMap<>();
        String defaultCategory = fallbackCategory.isEmpty() ? "" : fallbackCategory.get(0);

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String text = root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            // JSON 블록 추출 (```json ... ``` 형식 대응)
            if (text.contains("{")) {
                text = text.substring(text.indexOf("{"), text.lastIndexOf("}") + 1);
            }

            JsonNode mapping = objectMapper.readTree(text);
            mapping.fields().forEachRemaining(entry ->
                    result.put(entry.getKey(), entry.getValue().asText()));
        } catch (Exception e) {
            log.error("AI 응답 파싱 실패: {}", e.getMessage());
            productNames.forEach(name -> result.put(name, defaultCategory));
        }

        return result;
    }
}
