package com.goodee.beedan.config.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${file.web.path}")
    String webFilePath;
    @Value("${file.physical.path}")
    String physicalFilePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(webFilePath) // 웹에서 접근할 경로
                .addResourceLocations(physicalFilePath); // 실제 파일이 있는 물리 경로
    }
}