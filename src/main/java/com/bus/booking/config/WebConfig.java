package com.bus.booking.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cấu hình CORS và điều hướng Static Single-Page Application (SPA)
 * Tự động điều hướng URL thân thiện (không cần gõ đuôi .html) sang các file giao diện tĩnh chuẩn trong static/
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

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/login").setViewName("forward:/login.html");
        registry.addViewController("/trips").setViewName("forward:/trips.html");
        registry.addViewController("/seat-selection").setViewName("forward:/seat-selection.html");
        registry.addViewController("/payment").setViewName("forward:/payment.html");
        registry.addViewController("/appointments").setViewName("forward:/appointments.html");
        registry.addViewController("/my-tickets").setViewName("forward:/my-tickets.html");
        registry.addViewController("/admin").setViewName("forward:/admin.html");
        registry.addViewController("/operator").setViewName("forward:/operator.html");
    }
}
