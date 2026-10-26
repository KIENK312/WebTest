package com.bus.booking.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cấu hình CORS ở tầng Spring MVC (WebMvcConfigurer).
 * 
 * Áp dụng cho các request đi qua DispatcherServlet đến các REST Controller.
 * Hỗ trợ các client Frontend chạy trên Live Server (port 5500), React/Next (port 3000) hoặc Vite (port 5173).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 1. Cho phép các Origins từ Live Server (VS Code), Vite và React dev server
                .allowedOrigins(
                        "http://localhost:5500",
                        "http://127.0.0.1:5500",
                        "http://localhost:3000",
                        "http://localhost:5173"
                )
                // 2. Cho phép đầy đủ các HTTP Method chuẩn RESTful
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                // 3. Cho phép các Headers cần thiết khi gửi kèm JWT hoặc JSON payload
                .allowedHeaders(
                        "Authorization",
                        "Content-Type",
                        "X-Requested-With",
                        "Accept",
                        "Origin"
                )
                // 4. Cho phép Frontend đọc được Header Authorization trả về từ Server
                .exposedHeaders("Authorization")
                // 5. Bật allowCredentials để hỗ trợ gửi kèm Cookie/Credentials/Authorization
                .allowCredentials(true)
                // 6. Thời gian cache kết quả Preflight request tại trình duyệt (1 giờ)
                .maxAge(3600L);
    }
}
