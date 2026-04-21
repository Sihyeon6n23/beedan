package com.goodee.beedan.controller.root;

import com.goodee.beedan.dto.root.scheduler.SchedulerSettingDto;
import com.goodee.beedan.service.root.SchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class SchedulerController {

    private final SchedulerService schedulerService;

    @GetMapping("/root/scheduler")
    public String getScheduler(Model model)  {
        model.addAttribute("setting", schedulerService.getSchedulerSetting());

        return "/root/scheduler/scheduler-setting";
    }

    @PostMapping("/root/scheduler/save")
    public String postScheduler(@ModelAttribute SchedulerSettingDto schedulerSettingDto)  {

        // 폼에서 넘어오지 않는 lastRunTime 필드들은 현재 캐시의 최신 값으로 세팅
        SchedulerSettingDto current = schedulerService.getSchedulerSetting();
        schedulerSettingDto.setLastCrawlingRunTime(current.getLastCrawlingRunTime());
        schedulerSettingDto.setLastExchangeRateRunTime(current.getLastExchangeRateRunTime());
        schedulerSettingDto.setLastGradeResolveRunTime(current.getLastGradeResolveRunTime());
        schedulerSettingDto.setLastChatAutoCloseRunTime(current.getLastChatAutoCloseRunTime());
        schedulerSettingDto.setLastPopularStockUpdateTime(current.getLastPopularStockUpdateTime());
        schedulerSettingDto.setLastNewStockUpdateTime(current.getLastNewStockUpdateTime());
        schedulerSettingDto.setLastUnipassRunTime(current.getLastUnipassRunTime());
        schedulerSettingDto.setLastShipmentSyncRunTime(current.getLastShipmentSyncRunTime());

        schedulerService.saveSchedulerSetting(schedulerSettingDto); // 파일에 저장

        return "redirect:/root/scheduler"; // 저장 후 다시 페이지로 이동
    }
}
