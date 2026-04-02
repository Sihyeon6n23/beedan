package com.goodee.beedan.controller.main;

import com.goodee.beedan.client.weather.WeatherClient;
import com.goodee.beedan.dto.root.utility.UtilitySettingDto;
import com.goodee.beedan.dto.stock.StockListDto;
import com.goodee.beedan.dto.weather.WeatherResponseDto;
import com.goodee.beedan.service.news.NewsService;
import com.goodee.beedan.service.root.UtilityService;
import com.goodee.beedan.service.stock.StockService;
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
    private final NewsService newsService;
    private final StockService stockService;

    @GetMapping("/")
    public String getMain(Model model) {

        // 날씨 정보 호출
        List<Map<String, Object>> weatherList = weatherService.getWeatherList();
        model.addAttribute("weatherList", weatherList);

        // 뉴스 목록 가져오기 1
        List<Map<String, String>> leftNewsList = newsService.getLeftNewsList();
        model.addAttribute("leftNewsList", leftNewsList);
        // 뉴스 목록 가져오기 2
        List<Map<String, String>> rightNewsList = newsService.getRightNewsList();
        model.addAttribute("rightNewsList", rightNewsList);

        // 전 월 인기상품 전시
        
        // 최근 등록 상품 전시
        List<StockListDto> newStockList = stockService.findNewStocks();
        model.addAttribute("newStockList", newStockList);

        return "main/index";
    }
}
