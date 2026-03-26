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

    @Value("${unipass.api.key:NONE}")
    private String apiKey;

    @Value("${unipass.api.url:https://unipass.customs.go.kr:38010/ext/rest/cargCsclPrgsInfoQry/retrieveCargCsclPrgsInfo}")
    private String apiUrl;

    public String getCargoStatus(String hblNo, String blYear) {
        RestTemplate restTemplate = new RestTemplate();

        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        // api 문서 확인할 것
        String fullUrl = apiUrl + "?crkyCn=" + apiKey.trim()
                + "&hblNo=" + hblNo.trim()
                + "&blYy=" + blYear.trim();

        System.out.println("호출 URL: " + fullUrl);

        try {
            String response = restTemplate.getForObject(fullUrl, String.class);

            String jsonResult = xmlToJsonService.convertXmlToJson(response);
            log.info("최종 변환된 JSON 결과:\n{}", jsonResult);

            // 3. 특정 필드 파싱 확인 (디버깅용)
            xmlToJsonService.logExtractData(response);

            return jsonResult;
        } catch (Exception e) {
            return " 호출 실패: " + e.getMessage();
        }
    }
}
