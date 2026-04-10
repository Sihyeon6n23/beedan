package com.goodee.beedan.service.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnipassService {
    private final XmlToJsonService xmlToJsonService;

    @Value("${unipass.api.key:}")
    private String apiKey;

    @Value("${unipass.api.url:}")
    private String apiUrl;

    public String getCargoStatus(String hblNo, String blYear) {
        RestTemplate restTemplate = new RestTemplate();

        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        // api 문서 확인할 것
        String fullUrl = apiUrl + "?crkyCn=" + apiKey.trim()
                + "&hblNo=" + hblNo.trim()
                + "&blYy=" + blYear.trim();

        try {
            String response = restTemplate.getForObject(fullUrl, String.class);

            // String jsonResult = xmlToJsonService.convertXmlToJson(response); 전체 json 데이터 필요시 사용0
            return xmlToJsonService.extractProgressStatus(response);
        } catch (Exception e) {
            log.error("UNIPASS API 호출 실패 (HBL: {}): {}", hblNo, e.getMessage());
            return null;
        }
    }
}
