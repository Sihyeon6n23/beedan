package com.goodee.beedan.service.root;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goodee.beedan.dto.root.security.SecurityPolicyDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityService {
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String REDIS_KEY = "setting:security";

    @PostConstruct
    public void init() {
        if (redisTemplate.opsForValue().get(REDIS_KEY) == null) {
            SecurityPolicyDto initial;
            try {
                ClassPathResource resource = new ClassPathResource("security-policy.json");
                initial = objectMapper.readValue(resource.getInputStream(), SecurityPolicyDto.class);
                log.info("보안 정책을 JSON 파일에서 Redis로 초기 로딩했습니다.");
            } catch (Exception e) {
                initial = new SecurityPolicyDto();
                log.info("보안 정책을 기본값으로 Redis에 초기화했습니다.");
            }
            redisTemplate.opsForValue().set(REDIS_KEY, initial);
        }
    }

    public SecurityPolicyDto getSecPolDto() {
        Object value = redisTemplate.opsForValue().get(REDIS_KEY);
        if (value == null) {
            return new SecurityPolicyDto();
        }
        return objectMapper.convertValue(value, SecurityPolicyDto.class);
    }

    public SecurityPolicyDto getCachedPolicy() {
        return getSecPolDto();
    }

    public void saveSecurityPolicyDto(SecurityPolicyDto secPolDto) {
        redisTemplate.opsForValue().set(REDIS_KEY, secPolDto);
        log.info("보안 정책이 성공적으로 저장되었습니다.");
    }
}