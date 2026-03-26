package com.goodee.beedan.service.root;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goodee.beedan.dto.root.security.SecurityPolicyDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityService {
    private final ObjectMapper objectMapper;
    public SecurityPolicyDto readSecPolDto() throws IOException {
        String rootPath = System.getProperty("user.dir");
        File file = new File(rootPath, "security_policy.json");
        log.info("{}",file);

        if (!file.exists()) {
            log.info("{}","파일이 없습니다.");
            return new SecurityPolicyDto();
        }

        return objectMapper.readValue(file, SecurityPolicyDto.class);
    }

    public void saveSecurityPolicyDto(SecurityPolicyDto secPolDto) throws IOException {
        File file = new File("./security_policy.json");
        log.info("{}", secPolDto);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, secPolDto);
    }
}
