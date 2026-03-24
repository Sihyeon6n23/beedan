package com.goodee.beedan.service.order;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
public class UnipassService {
    @Value("${unipass.api.key}")
    private String apiKey;

    @Value("${unipass.api.url}")
    private String apiUrl;

    public String getCargoStatus(String hblNo, String blYear) {
        RestTemplate restTemplate = new RestTemplate();

        // api 문서 확인할 것
        String fullUrl = apiUrl + "?crkyCn=" + apiKey.trim()
                + "&hblNo=" + hblNo.trim()
                + "&blYy=" + blYear.trim();

        System.out.println("호출 URL: " + fullUrl);

        try {
            String response = restTemplate.getForObject(fullUrl, String.class);
            return response;
        } catch (Exception e) {
            return " 호출 실패: " + e.getMessage();
        }
    }
}
