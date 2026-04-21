package com.goodee.beedan.service.root;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goodee.beedan.dto.root.utility.UtilitySettingDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UtilityService {

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String REDIS_KEY = "setting:utility";

    @PostConstruct
    public void init() {
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
        } catch (Exception e) {
            log.warn("Redis 연결 실패로 유틸리티 설정 초기화를 건너뜁니다: {}", e.getMessage());
            return;
        }

        if (redisTemplate.opsForValue().get(REDIS_KEY) == null) {
            UtilitySettingDto initial;
            try {
                ClassPathResource resource = new ClassPathResource("utility-setting.json");
                initial = objectMapper.readValue(resource.getInputStream(), UtilitySettingDto.class);
                log.info("유틸리티 설정을 JSON 파일에서 Redis로 초기 로딩.");
            } catch (Exception e) {
                initial = new UtilitySettingDto();
                log.info("유틸리티 설정을 기본값으로 Redis에 초기화.");
            }
            redisTemplate.opsForValue().set(REDIS_KEY, initial);
        }
    }

    public UtilitySettingDto getUtilitySetting() {
        Object value = redisTemplate.opsForValue().get(REDIS_KEY);
        if (value == null) {
            return new UtilitySettingDto();
        }
        return objectMapper.convertValue(value, UtilitySettingDto.class);
    }

    public void saveUtilitySetting(UtilitySettingDto utilitySettingDto) {
        redisTemplate.opsForValue().set(REDIS_KEY, utilitySettingDto);
        log.info("유틸리티 설정 Redis에 저장 완료.");
    }
}
