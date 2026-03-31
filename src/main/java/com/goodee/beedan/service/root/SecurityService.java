package com.goodee.beedan.service.root;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goodee.beedan.dto.root.security.SecurityPolicyDto;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
@Getter
public class SecurityService {
    private final ObjectMapper objectMapper;
    private SecurityPolicyDto cachedPolicy;
    private final String filePath = "src/main/resources/security-policy.json";

    @PostConstruct
    public void init() throws IOException{
        File file = new File(filePath);
        if (file.exists()) {
            this.cachedPolicy = objectMapper.readValue(file, SecurityPolicyDto.class);
        } else {
            this.cachedPolicy = new SecurityPolicyDto();
        }
    }

    public SecurityPolicyDto getSecPolDto() {
        return cachedPolicy;
    }

    public void saveSecurityPolicyDto(SecurityPolicyDto secPolDto) throws IOException {
        File file = new File(filePath);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, secPolDto);

        this.cachedPolicy = secPolDto;
    }
}
