package com.goodee.beedan.service.root;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goodee.beedan.dto.root.scheduler.SchedulerSettingDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {
    private final ObjectMapper objectMapper;
    private volatile SchedulerSettingDto cachedSetting;
    private final String filePath = "src/main/resources/scheduler-setting.json";

    @PostConstruct
    public void init() throws IOException{
        File file = new File(filePath);
        if (file.exists()) {
            cachedSetting = objectMapper.readValue(file, SchedulerSettingDto.class);
        } else {
            cachedSetting = new SchedulerSettingDto();
        }
    }

    public SchedulerSettingDto getSchedulerSetting() {
        return cachedSetting;
    }

    public void saveSchedulerSetting(SchedulerSettingDto schedulerSettingDto) throws IOException {
        File file = new File(filePath);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, schedulerSettingDto);
        cachedSetting = schedulerSettingDto;
    }
}
