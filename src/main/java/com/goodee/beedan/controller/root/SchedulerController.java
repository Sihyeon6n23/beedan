package com.goodee.beedan.controller.root;

import com.goodee.beedan.dto.root.scheduler.SchedulerSettingDto;
import com.goodee.beedan.service.root.SchedulerService;
import com.goodee.beedan.service.stock.StockDisplayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class SchedulerController {

    private final SchedulerService schedulerService;
    private final StockDisplayService stockDisplayService;

    @GetMapping("/root/scheduler")
    public String getScheduler(Model model) throws IOException {
        model.addAttribute("setting", schedulerService.getSchedulerSetting());

        return "/root/scheduler/scheduler-setting";
    }

    @PostMapping("/root/scheduler/save")
    public String postScheduler(@ModelAttribute SchedulerSettingDto schedulerSettingDto) throws IOException {

        schedulerService.saveSchedulerSetting(schedulerSettingDto); // 파일에 저장

        return "redirect:/root/scheduler"; // 저장 후 다시 페이지로 이동
    }

    @PostMapping("/root/scheduler/refresh/popular")
    @ResponseBody
    public ResponseEntity<Map<String, String>> refreshPopularStocks() throws IOException {
        stockDisplayService.refreshPopularStocks();

        SchedulerSettingDto setting = schedulerService.getSchedulerSetting();
        setting.setLastPopularStockUpdateTime(LocalDateTime.now().toString());
        schedulerService.saveSchedulerSetting(setting);

        return ResponseEntity.ok(Map.of("status", "success"));
    }

    @PostMapping("/root/scheduler/refresh/new")
    @ResponseBody
    public ResponseEntity<Map<String, String>> refreshNewStocks() throws IOException {
        stockDisplayService.refreshNewStocks();

        SchedulerSettingDto setting = schedulerService.getSchedulerSetting();
        setting.setLastNewStockUpdateTime(LocalDateTime.now().toString());
        schedulerService.saveSchedulerSetting(setting);

        return ResponseEntity.ok(Map.of("status", "success"));
    }
}
