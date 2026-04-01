package com.goodee.beedan.controller.root;

import com.goodee.beedan.dto.root.scheduler.SchedulerSettingDto;
import com.goodee.beedan.service.root.SchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
public class SchedulerController {

    private final SchedulerService schedulerService;

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


}
