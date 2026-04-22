package com.goodee.beedan.service.root;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goodee.beedan.dto.root.utility.UtilitySettingDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@RequiredArgsConstructor
@Slf4j
public class UtilityService {

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REDIS_KEY = "setting:utility";

    @Value("${setting.file.develop.path}")
    private String localFilePath;
    private String localFileFullPath;

    private UtilitySettingDto localCache;

    @PostConstruct
    public void init() {
        localFileFullPath = localFilePath + "utility-setting-external.json";
        // 1순위: Redis 로드 시도
        if (loadFromRedis()) return;

        // 2순위: 로컬 물리 파일 로드 시도 (Redis 장애 시 백업)
        if (loadFromLocalFile()) return;

        // 3순위: 프로젝트 내 기본 JSON 로드 (최후의 보루)
        loadFromClassPath();
    }

    // --- 로딩 로직 상세 ---

    private boolean loadFromRedis() {
        try {
            Object value = redisTemplate.opsForValue().get(REDIS_KEY);
            if (value != null) {
                this.localCache = objectMapper.convertValue(value, UtilitySettingDto.class);
                log.info("유틸리티 설정을 Redis에서 로드했습니다.");
                return true;
            }
        } catch (Exception e) {
            log.warn("Redis 연결 불가. 로컬 백업을 확인합니다.");
        }
        return false;
    }

    private boolean loadFromLocalFile() {
        File file = new File(localFileFullPath);
        if (file.exists()) {
            try {
                this.localCache = objectMapper.readValue(file, UtilitySettingDto.class);
                log.info("로컬 파일({})에서 유틸리티 설정을 로드했습니다.", localFileFullPath);
                return true;
            } catch (Exception e) {
                log.error("로컬 유틸리티 설정 파일 읽기 실패", e);
            }
        }
        return false;
    }

    private void loadFromClassPath() {
        try {
            ClassPathResource resource = new ClassPathResource("utility-setting.json");
            this.localCache = objectMapper.readValue(resource.getInputStream(), UtilitySettingDto.class);
            log.info("기본 유틸리티 설정(ClassPath)을 로드했습니다.");
        } catch (Exception e) {
            this.localCache = new UtilitySettingDto();
            log.warn("기본 설정 파일이 없어 빈 객체로 초기화합니다.");
        }
    }

    // --- 실행 로직 ---

    public UtilitySettingDto getUtilitySetting() {
        // 매번 Redis를 확인하여 변경사항 반영 (실패 시 localCache 반환)
        loadFromRedis();
        return this.localCache != null ? this.localCache : new UtilitySettingDto();
    }

    public synchronized void saveUtilitySetting(UtilitySettingDto dto) {
        // 1. 메모리 업데이트
        this.localCache = dto;

        // 2. 로컬 파일 시스템에 즉시 저장 (영속성 확보)
        try {
            objectMapper.writeValue(new File(localFileFullPath), dto);
            log.info("유틸리티 설정이 로컬 파일에 저장되었습니다.");
        } catch (Exception e) {
            log.error("로컬 파일 저장 실패", e);
        }

        // 3. Redis 동기화 시도
        try {
            redisTemplate.opsForValue().set(REDIS_KEY, dto);
            log.info("유틸리티 설정이 Redis에 저장되었습니다.");
        } catch (Exception e) {
            log.warn("Redis 접속 실패로 인해 Redis 동기화에 실패했습니다.");
        }
    }
}