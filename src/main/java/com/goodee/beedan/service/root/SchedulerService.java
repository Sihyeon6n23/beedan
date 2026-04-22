package com.goodee.beedan.service.root;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goodee.beedan.dto.root.scheduler.SchedulerSettingDto;
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
public class SchedulerService {
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REDIS_KEY = "setting:scheduler";
    // 서버 환경에 맞게 경로 수정 (예: ./config/ 또는 /app/config/)
    @Value("${setting.file.develop.path}")
    private String localFilePath;
    private String localFileFullPath;

    private SchedulerSettingDto localCache;

    @PostConstruct
    public void init() {
        File folder = new File(localFilePath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        localFileFullPath = localFilePath + "scheduler-setting-external.json";
        // 1. 우선순위 1: Redis에서 로드 시도
        if (loadFromRedis()) return;

        // 2. 우선순위 2: 로컬 외부 파일에서 로드 시도 (Redis 실패 시)
        if (loadFromLocalFile()) return;

        // 3. 우선순위 3: ClassPath 내 기본 JSON 파일 로드
        loadFromClassPath();
    }

    // --- 로드 로직들 ---

    private boolean loadFromRedis() {
        try {
            Object value = redisTemplate.opsForValue().get(REDIS_KEY);
            if (value != null) {
                this.localCache = objectMapper.convertValue(value, SchedulerSettingDto.class);
                log.info("Redis에서 설정을 로드했습니다.");
                return true;
            }
        } catch (Exception e) {
            log.warn("Redis 연결 실패로 데이터를 가져오지 못했습니다.");
        }
        return false;
    }

    private boolean loadFromLocalFile() {
        File file = new File(localFileFullPath);
        if (file.exists()) {
            try {
                this.localCache = objectMapper.readValue(file, SchedulerSettingDto.class);
                log.info("로컬 파일({})에서 설정을 로드했습니다.", localFileFullPath);
                return true;
            } catch (Exception e) {
                log.error("로컬 파일 읽기 실패", e);
            }
        }
        return false;
    }

    private void loadFromClassPath() {
        try {
            ClassPathResource resource = new ClassPathResource("scheduler-setting.json");
            this.localCache = objectMapper.readValue(resource.getInputStream(), SchedulerSettingDto.class);
            log.info("ClassPath 기본 설정파일을 로드했습니다.");
        } catch (Exception e) {
            this.localCache = new SchedulerSettingDto();
            log.warn("기본 설정파일이 없어 빈 객체를 생성합니다.");
        }
    }

    // --- 비즈니스 로직 ---

    public SchedulerSettingDto getSchedulerSetting() {
        // 실시간으로 Redis 확인 (성공 시 캐시 업데이트)
        loadFromRedis();
        return this.localCache != null ? this.localCache : new SchedulerSettingDto();
    }

    public synchronized void saveSchedulerSetting(SchedulerSettingDto dto) {
        // 1. 메모리 업데이트
        this.localCache = dto;

        // 2. 로컬 파일 시스템에 저장 (Redis 장애 대비 영구 저장)
        try {
            objectMapper.writeValue(new File(localFileFullPath), dto);
            log.info("로컬 파일에 설정 저장 완료.");
        } catch (Exception e) {
            log.error("로컬 파일 저장 중 오류 발생", e);
        }

        // 3. Redis 저장 시도
        try {
            redisTemplate.opsForValue().set(REDIS_KEY, dto);
            log.info("Redis에 설정 저장 완료.");
        } catch (Exception e) {
            log.warn("Redis 저장 실패 (로컬 파일에는 저장됨)");
        }
    }
}