package com.goodee.beedan.dto.root.utility;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UtilitySettingDto {

    // 1. 날씨 정보 dto
    @JsonProperty("weather_location_list")
    private List<WeatherLocation> weatherLocationList = new ArrayList<>(List.of(
            new WeatherLocation("Las Vegas", "US"),
            new WeatherLocation("Osaka", "Osaka, JP"),
            new WeatherLocation("Tokyo", "JP"),
            new WeatherLocation("Seoul", "Seoul, KR")
    ));

    @Data
    public static class WeatherLocation {
        @JsonProperty("city_name")
        private String cityName;

        @JsonProperty("region")
        private String region;

        public WeatherLocation() {}

        public WeatherLocation(String cityName, String region) {
            this.cityName = cityName;
            this.region = region;
        }
    }

    // 2. 뉴스 키워드
    @JsonProperty("news_keyword_left")
    private String newsKeywordLeft = "세계 패션 의류";

    @JsonProperty("news_keyword_right")
    private String newsKeywordRight = "세계 무역 관세 환율";

}
