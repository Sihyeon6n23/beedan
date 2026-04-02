package com.goodee.beedan.controller.main;

import com.goodee.beedan.client.weather.WeatherClient;
import com.goodee.beedan.dto.root.utility.UtilitySettingDto;
import com.goodee.beedan.dto.weather.WeatherResponseDto;
import com.goodee.beedan.service.root.UtilityService;
import com.goodee.beedan.service.weather.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MainController {

    private final WeatherService weatherService;

    @GetMapping("/")
    public String getMain(Model model) {

        // 날씨 정보 호출
        List<Map<String, Object>> weatherList = weatherService.getWeatherList();
        model.addAttribute("weatherList", weatherList);


        return "main/index";
    }
}
