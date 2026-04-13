package com.goodee.beedan.config.mapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectMapperConfig {

    @Bean
    public ObjectMapperConfig objectMapper() { // 2. 반환 타입은 Jackson의 ObjectMapper
        ObjectMapperConfig mapper = new ObjectMapperConfig();

        return mapper;
    }
}
