package com.goodee.beedan.dto.root.utility;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class UtilitySettingDto {

    // 1. 날씨 정보 dto
    @JsonProperty("weather_location_list")
    private List<WeatherLocation> weatherLocationList;

    @Data
    public static class WeatherLocation {
        @JsonProperty("city_name")
        private String cityName;

        @JsonProperty("region")
        private String region;
    }

    // 2. 뉴스 키워드
    @JsonProperty("news_keyword_left")
    private String newsKeywordLeft;

    @JsonProperty("news_keyword_right")
    private String newsKeywordRight;

}
