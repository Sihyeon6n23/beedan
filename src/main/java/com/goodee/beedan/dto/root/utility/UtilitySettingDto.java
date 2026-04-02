package com.goodee.beedan.dto.root.utility;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class UtilitySettingDto {

    @JsonProperty("weather_location_list")
    private List<WeatherLocation> weatherLocationList;

    @Data
    public static class WeatherLocation {
        @JsonProperty("city_name")
        private String cityName;

        @JsonProperty("region")
        private String region;
    }
}
