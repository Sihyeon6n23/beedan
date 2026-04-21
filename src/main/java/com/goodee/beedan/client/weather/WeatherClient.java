package com.goodee.beedan.client.weather;

import com.goodee.beedan.dto.weather.GeoSearchResultDto;
import com.goodee.beedan.dto.weather.WeatherResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherClient {

    @Qualifier("webClient")
    private final WebClient webClient;

    @Value("${openweather.api.key}")
    private String apiKey;

    public List<GeoSearchResultDto> searchCity(String query) {
        String trimmed = query.trim();
        String noSpaces = trimmed.replaceAll("\\s+", "");

        // 원본 쿼리로 검색
        List<GeoSearchResultDto> results = new ArrayList<>(callGeoApi(trimmed));

        // 공백 제거 버전이 다르면 추가 검색
        if (!trimmed.equals(noSpaces)) {
            results.addAll(callGeoApi(noSpaces));
        }

        // 중복 제거 (name+country 기준)
        return results.stream()
                .collect(Collectors.toMap(
                        d -> (d.getName() + "," + d.getCountry()).toLowerCase(),
                        d -> d,
                        (a, b) -> a,
                        LinkedHashMap::new
                ))
                .values().stream()
                .collect(Collectors.toList());
    }

    private List<GeoSearchResultDto> callGeoApi(String q) {
        try {
            List<GeoSearchResultDto> result = webClient.get()
                    .uri("https://api.openweathermap.org/geo/1.0/direct?q={q}&limit=5&appid={key}",
                            q, apiKey)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<GeoSearchResultDto>>() {})
                    .block();
            return result != null ? result : Collections.emptyList();
        } catch (Exception e) {
            log.warn("Geocoding 검색 실패: {}", q, e);
            return Collections.emptyList();
        }
    }

    public WeatherResponseDto fetchWeather(String cityName) {
        return webClient.get()
                .uri("https://api.openweathermap.org/data/2.5/weather?q={q}&units=metric&appid={key}",
                        cityName, apiKey)
                .retrieve()
                .bodyToMono(WeatherResponseDto.class)
                .block();
    }
}
