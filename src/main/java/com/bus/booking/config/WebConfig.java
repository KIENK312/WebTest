package com.bus.booking.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cấu hình CORS và Web MVC cho hệ thống Quản lý Bán vé Xe khách & Lịch hẹn tư vấn.
 * Sử dụng Thymeleaf + PRG Pattern chuẩn mực.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders(
                        "Authorization",
                        "Content-Type",
                        "X-Requested-With",
                        "Accept",
                        "Origin"
                )
                .exposedHeaders("Authorization")
                .allowCredentials(true)
                .maxAge(3600L);
    }
}

