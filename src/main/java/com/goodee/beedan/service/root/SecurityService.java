package com.goodee.beedan.service.root;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goodee.beedan.dto.root.security.SecurityPolicyDto;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
@Getter
public class SecurityService {
    private final ObjectMapper objectMapper;
    private SecurityPolicyDto cachedPolicy;

    // 주의: 아래 '💡 실무 팁'을 꼭 읽어주세요!
    private final String filePath = "src/main/resources/security-policy.json";

    @PostConstruct
    public void init() {
        File file = new File(filePath);
        try {
            if (file.exists()) {
                this.cachedPolicy = objectMapper.readValue(file, SecurityPolicyDto.class);
                log.info("기존 보안 정책 파일을 로드했습니다.");
            } else {
                File parentDir = file.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }

                this.cachedPolicy = new SecurityPolicyDto();
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, this.cachedPolicy);
                log.info("새로운 보안 정책 파일을 생성했습니다: {}", file.getAbsolutePath());
            }
        } catch (IOException e) {
            log.error("보안 정책 초기화 중 오류 발생: {}", e.getMessage(), e);
            this.cachedPolicy = new SecurityPolicyDto();
        }
    }

    public SecurityPolicyDto getSecPolDto() {
        return cachedPolicy;
    }

    public void saveSecurityPolicyDto(SecurityPolicyDto secPolDto) throws IOException {
        File file = new File(filePath);

        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, secPolDto);
        this.cachedPolicy = secPolDto;
        log.info("보안 정책이 성공적으로 저장되었습니다.");
    }
}