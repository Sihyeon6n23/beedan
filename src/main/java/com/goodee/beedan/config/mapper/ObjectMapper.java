package com.goodee.beedan.config.mapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectMapper {

    @Bean
    public ObjectMapper objectMapper() { // 2. 반환 타입은 Jackson의 ObjectMapper
        ObjectMapper mapper = new ObjectMapper();

        return mapper;
    }
}
