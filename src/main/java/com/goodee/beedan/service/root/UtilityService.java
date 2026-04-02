package com.goodee.beedan.service.root;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goodee.beedan.dto.root.utility.UtilitySettingDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UtilityService {

    private final ObjectMapper objectMapper;
    private UtilitySettingDto cachedSetting;
    private final String filePath = "src/main/resources/utility-setting.json";

    @PostConstruct
    public void init() throws IOException {
        File file = new File(filePath);
        if (file.exists()) {
            cachedSetting = objectMapper.readValue(file, UtilitySettingDto.class);
        } else {
            cachedSetting = new UtilitySettingDto();
        }
    }

    public UtilitySettingDto getUtilitySetting() {
        return cachedSetting;
    }

    public void saveUtilitySetting(UtilitySettingDto utilitySettingDto) throws IOException {
        File file = new File(filePath);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, utilitySettingDto);
        cachedSetting = utilitySettingDto;
    }
}
