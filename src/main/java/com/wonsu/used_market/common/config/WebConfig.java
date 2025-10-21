package com.wonsu.used_market.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /uploads/** 요청이 실제 로컬 "uploads/" 폴더로 연결되도록 설정
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
