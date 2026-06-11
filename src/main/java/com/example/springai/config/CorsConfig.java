package com.example.springai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 原本的 /api/** 放行
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);

        // 🔥 新增：專門放行 LINE Webhook 的 /callback 路由
        registry.addMapping("/callback/**")
                .allowedOriginPatterns("*")
                .allowedMethods("POST", "OPTIONS") // LINE 主要使用 POST
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
