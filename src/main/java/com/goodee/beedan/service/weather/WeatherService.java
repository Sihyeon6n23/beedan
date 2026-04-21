package com.goodee.beedan.service.weather;

import com.goodee.beedan.client.weather.WeatherClient;
import com.goodee.beedan.dto.root.utility.UtilitySettingDto;
import com.goodee.beedan.dto.weather.WeatherResponseDto;
import com.goodee.beedan.service.root.UtilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

    private final WeatherClient weatherClient;
    private final UtilityService utilityService;

    private static final Map<String, String> ICON_MAP = Map.of(
            "Clear", "sunny",
            "Clouds", "cloud",
            "Rain", "rainy",
            "Drizzle", "rainy",
            "Thunderstorm", "thunderstorm",
            "Snow", "weather_snowy",
            "Mist", "mist",
            "Fog", "mist",
            "Haze", "mist"
    );

    public List<Map<String, Object>> getWeatherList() {
        List<Map<String, Object>> weatherList = new ArrayList<>();
        UtilitySettingDto setting = utilityService.getUtilitySetting();

        if (setting.getWeatherLocationList() != null) {
            for (UtilitySettingDto.WeatherLocation loc : setting.getWeatherLocationList()) {
                try {
                    WeatherResponseDto weather = weatherClient.fetchWeather(loc.getCityName());
                    String condition = weather.getWeather().isEmpty() ? "Clear" : weather.getWeather().get(0).getMain();

                    Map<String, Object> card = new LinkedHashMap<>();
                    card.put("cityName", loc.getCityName());
                    card.put("region", loc.getRegion());
                    card.put("temp", Math.round(weather.getMain().getTemp()));
                    card.put("humidity", weather.getMain().getHumidity());
                    card.put("windSpeed", weather.getWind().getSpeed());
                    card.put("icon", ICON_MAP.getOrDefault(condition, "partly_cloudy_day"));
                    weatherList.add(card);
                } catch (Exception e) {
                    log.warn("날씨 조회 실패: {}", loc.getCityName(), e);
                }
            }
        }

        return weatherList;
    }
}
