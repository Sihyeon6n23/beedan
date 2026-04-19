package com.goodee.beedan.service.order;

import com.goodee.beedan.dto.order.TrackingResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service @Slf4j
public class UnipassService {
    private final XmlToJsonService xmlToJsonService;

    @Qualifier("unipassRestTemplate")
    private final RestTemplate restTemplate;

    @Value("${unipass.api.key:}")
    private String apiKey;

    @Value("${unipass.api.url:}")
    private String apiUrl;

    public UnipassService(RestTemplateBuilder builder, XmlToJsonService xmlToJsonService) {
        this.restTemplate = builder
                .additionalMessageConverters(new StringHttpMessageConverter(StandardCharsets.UTF_8))
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(5))
                .build();
        this.xmlToJsonService = xmlToJsonService;
    }


    @Cacheable(value = "shipment:customs", key = "#hblNo + '_' + #blYear", unless = "#result == null")
    public String getCargoStatus(String hblNo, String blYear) {  // 사용자용
        return fetchUnipassRawData(hblNo, blYear);
    }


    private String fetchUnipassRawData(String hblNo, String blYear) {
        String fullUrl = apiUrl + "?crkyCn=" + apiKey.trim() + "&hblNo=" + hblNo.trim() + "&blYy=" + blYear.trim();

        try {
            String response = restTemplate.getForObject(fullUrl, String.class);
            return xmlToJsonService.extractProgressStatus(response);
        } catch (Exception e) {
            log.error("UNIPASS API 호출 실패: {}", e.getMessage());
            return null;
        }
    }

    @CachePut(value = "shipment:customs", key = "#hblNo + '_' + #blYear", unless = "#result == null") // @CachePut이 걸려있어 최신 상태로 캐시가 갱신됨
    @CacheEvict(value = "shipment:customs", key = "'timeline_' + #hblNo + '_' + #blYear") // 타임라인 캐시도 같이 날려서 다음 조회 시 최신 타임라인이 뜨도록 함
    public String updateCargoStatusForScheduler(String hblNo, String blYear) {
        return fetchUnipassRawData(hblNo, blYear); // 실제 API 호출 및 캐시 강제 갱신
    }

    @Cacheable(value = "shipment:customs", key = "'timeline_' + #hblNo + '_' + #blYear", unless = "#result == null || #result.isEmpty()")
    public List<TrackingResponseDto.TrackingDetail> getCustomsTimeline(String hblNo, String blYear) {  // shipment 안에 통관 타임라인 포함시키기 위해 사용
        String fullUrl = apiUrl + "?crkyCn=" + apiKey.trim() + "&hblNo=" + hblNo.trim() + "&blYy=" + blYear.trim();

        try {
            log.info("==> [Redis Cache Miss] 통관 타임라인 API 호출: {}", hblNo);
            String response = restTemplate.getForObject(fullUrl, String.class);

            return xmlToJsonService.extractCustomsTimeline(response);
        } catch (Exception e) {
            log.error("통관 타임라인 조회 실패: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
