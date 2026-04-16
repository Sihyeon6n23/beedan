package com.goodee.beedan.service.root;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goodee.beedan.dto.root.scheduler.SchedulerSettingDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String REDIS_KEY = "setting:scheduler";

    @PostConstruct
    public void init() {
        if (redisTemplate.opsForValue().get(REDIS_KEY) == null) {
            SchedulerSettingDto initial;
            try {
                ClassPathResource resource = new ClassPathResource("scheduler-setting.json");
                initial = objectMapper.readValue(resource.getInputStream(), SchedulerSettingDto.class);
                log.info("스케줄러 설정을 JSON 파일에서 Redis로 초기 로딩.");
            } catch (Exception e) {
                initial = new SchedulerSettingDto();
                log.info("스케줄러 설정을 기본값으로 Redis에 초기화.");
            }
            redisTemplate.opsForValue().set(REDIS_KEY, initial);
        }
    }

    public SchedulerSettingDto getSchedulerSetting() {
        Object value = redisTemplate.opsForValue().get(REDIS_KEY);
        if (value == null) {
            return new SchedulerSettingDto();
        }
        return objectMapper.convertValue(value, SchedulerSettingDto.class);
    }

    public synchronized void saveSchedulerSetting(SchedulerSettingDto schedulerSettingDto) {
        redisTemplate.opsForValue().set(REDIS_KEY, schedulerSettingDto);
        log.info("스케쥴러 설정 Redis에 저장 완료.");
    }
}
