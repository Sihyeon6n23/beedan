package com.goodee.beedan.service.root;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goodee.beedan.dto.root.security.SecurityPolicyDto;
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
public class SecurityService {
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REDIS_KEY = "setting:security";

    @Value("${setting.file.develop.path}")
    private String localFilePath;
    private String localFileFullPath;

    private SecurityPolicyDto localCache;

    @PostConstruct
    public void init() {
        localFileFullPath = localFilePath + "security-policy-external.json";
        // 1단계: Redis 로드 시도
        if (loadFromRedis()) return;

        // 2단계: 로컬 외부 파일 로드 시도 (Redis 실패 시 백업 확인)
        if (loadFromLocalFile()) return;

        // 3단계: JAR 내 기본 설정 파일 로드 (최후의 보루)
        loadFromClassPath();
    }

    // --- 로딩 로직 상세 ---

    private boolean loadFromRedis() {
        try {
            Object value = redisTemplate.opsForValue().get(REDIS_KEY);
            if (value != null) {
                this.localCache = objectMapper.convertValue(value, SecurityPolicyDto.class);
                log.info("보안 정책을 Redis에서 성공적으로 로드했습니다.");
                return true;
            }
        } catch (Exception e) {
            log.warn("Redis 연결 불가 또는 데이터 없음. 로컬 백업을 확인합니다.");
        }
        return false;
    }

    private boolean loadFromLocalFile() {
        File file = new File(localFileFullPath);
        if (file.exists()) {
            try {
                this.localCache = objectMapper.readValue(file, SecurityPolicyDto.class);
                log.info("로컬 파일({})에서 보안 정책을 로드했습니다.", localFileFullPath);
                return true;
            } catch (Exception e) {
                log.error("로컬 보안 정책 파일 읽기 중 오류 발생", e);
            }
        }
        return false;
    }

    private void loadFromClassPath() {
        try {
            ClassPathResource resource = new ClassPathResource("security-policy.json");
            this.localCache = objectMapper.readValue(resource.getInputStream(), SecurityPolicyDto.class);
            log.info("기본 보안 정책(ClassPath)을 로드했습니다.");
        } catch (Exception e) {
            this.localCache = new SecurityPolicyDto();
            log.warn("기본 설정 파일을 찾을 수 없어 빈 객체로 초기화합니다.");
        }
    }

    // --- 비즈니스 메서드 ---

    public SecurityPolicyDto getSecPolDto() {
        // 호출 시마다 Redis 조회를 시도하여 최신화 (실패 시 localCache 유지)
        loadFromRedis();
        return this.localCache != null ? this.localCache : new SecurityPolicyDto();
    }

    public SecurityPolicyDto getCachedPolicy() {
        return getSecPolDto();
    }

    public synchronized void saveSecurityPolicyDto(SecurityPolicyDto secPolDto) {
        // 1. 메모리 즉시 반영
        this.localCache = secPolDto;

        // 2. 로컬 물리 파일에 저장 (영속성 보장)
        try {
            objectMapper.writeValue(new File(localFileFullPath), secPolDto);
            log.info("보안 정책이 로컬 파일에 저장되었습니다.");
        } catch (Exception e) {
            log.error("보안 정책 로컬 파일 저장 실패", e);
        }

        // 3. Redis 동기화 시도
        try {
            redisTemplate.opsForValue().set(REDIS_KEY, secPolDto);
            log.info("보안 정책이 Redis에 저장되었습니다.");
        } catch (Exception e) {
            log.warn("Redis 접속 실패로 인해 설정이 Redis에 반영되지 않았습니다. (로컬 파일은 안전함)");
        }
    }
}