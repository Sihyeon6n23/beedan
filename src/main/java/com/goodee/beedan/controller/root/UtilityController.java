package com.goodee.beedan.controller.root;

import com.goodee.beedan.client.weather.WeatherClient;
import com.goodee.beedan.dto.root.utility.UtilitySettingDto;
import com.goodee.beedan.dto.weather.GeoSearchResultDto;
import com.goodee.beedan.service.root.UtilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/root/utility")
public class UtilityController {

    private final UtilityService utilityService;
    private final WeatherClient weatherClient;

    @GetMapping
    public String getUtility(Model model) {
        model.addAttribute("setting", utilityService.getUtilitySetting());
        return "/root/utility/utility-setting";
    }

    @PostMapping("/save")
    public String saveUtility(@ModelAttribute UtilitySettingDto utilitySettingDto)  {
        utilityService.saveUtilitySetting(utilitySettingDto);

        return "redirect:/root/utility";
    }

    @GetMapping("/weather/search")
    @ResponseBody
    public List<GeoSearchResultDto> searchCity(@RequestParam("q") String query) {
        return weatherClient.searchCity(query);
    }


}
