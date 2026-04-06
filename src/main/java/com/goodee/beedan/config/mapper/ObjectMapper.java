package com.goodee.beedan.config.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectMapper {
    @Bean
    public ObjectMapper objectMapperConfig() {
        return new ObjectMapper();
    }
}
