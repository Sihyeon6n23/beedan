package com.goodee.beedan.controller.root;

import com.goodee.beedan.dto.root.scheduler.SchedulerSettingDto;
import com.goodee.beedan.service.root.SchedulerService;
import com.goodee.beedan.service.stock.StockDisplayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/root/scheduler")
@RequiredArgsConstructor
public class SchedulerApiController {

    private final SchedulerService schedulerService;
    private final StockDisplayService stockDisplayService;

    @PostMapping("/refresh/popular")
    public ResponseEntity<Map<String, String>> refreshPopularStocks() {
        stockDisplayService.refreshPopularStocks();

        SchedulerSettingDto setting = schedulerService.getSchedulerSetting();
        setting.setLastPopularStockUpdateTime(LocalDateTime.now().toString());
        schedulerService.saveSchedulerSetting(setting);

        return ResponseEntity.ok(Map.of("status", "success"));
    }

    @PostMapping("/refresh/new")
    public ResponseEntity<Map<String, String>> refreshNewStocks() {
        stockDisplayService.refreshNewStocks();

        SchedulerSettingDto setting = schedulerService.getSchedulerSetting();
        setting.setLastNewStockUpdateTime(LocalDateTime.now().toString());
        schedulerService.saveSchedulerSetting(setting);

        return ResponseEntity.ok(Map.of("status", "success"));
    }
}
