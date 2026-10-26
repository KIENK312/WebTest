package com.bus.booking.security;

import com.bus.booking.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Tắt CSRF vì sử dụng Stateless JWT Authentication
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Cấu hình CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 3. Xử lý ngoại lệ 401 Unauthorized và 403 Forbidden dạng JSON chuẩn
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                // 4. Quản lý phiên Stateless (không lưu session trên server)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 5. Phân quyền Endpoint theo Lambda DSL chuẩn Spring Security 6
                .authorizeHttpRequests(auth -> auth
                        // -----------------------------------------------------------------------------------------
                        // TẠI SAO PHẢI XỬ LÝ RIÊNG PRE-FLIGHT (OPTIONS) TRONG SECURITY FILTER CHAIN?
                        // Theo đặc tả W3C CORS & RFC 7231, khi Frontend gửi một "Non-Simple Request" 
                        // (chẳng hạn có Header tùy biến như "Authorization: Bearer <JWT>", "Content-Type: application/json",
                        // hoặc các HTTP Method như PUT, DELETE, PATCH), trình duyệt web sẽ TỰ ĐỘNG gửi một HTTP request
                        // với phương thức OPTIONS (gọi là Pre-flight Request) đến Server trước để kiểm tra quyền truy cập.
                        // 
                        // ĐIỂM MẤU CHỐT:
                        // 1. Preflight request (OPTIONS) KHÔNG BAO GIỜ mang theo thông tin xác thực (Credentials) 
                        //    hay Header Authorization của người dùng.
                        // 2. Spring Security Filter Chain chạy TRƯỚC Spring MVC (DispatcherServlet). Nếu ta không cho phép 
                        //    OPTIONS request đi qua một cách tường minh hoặc không tích hợp CorsFilter sớm trong Filter Chain,
                        //    Spring Security sẽ chặn đứng request OPTIONS với lỗi HTTP 401 Unauthorized hoặc 403 Forbidden.
                        // 3. Hậu quả: Trình duyệt nhận thấy Pre-flight bị từ chối sẽ lập tức hủy bỏ (abort) request chính, 
                        //    dẫn đến lỗi CORS màu đỏ trên console trình duyệt dù Endpoint đó đã đúng URL và đúng Token!
                        // -----------------------------------------------------------------------------------------
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Cho phép truy cập công khai tất cả file HTML/JS tĩnh (Vanilla JS frontend)
                        // Không yêu cầu xác thực với file tĩnh – JWT chỉ cần ở tầng API
                        .requestMatchers(
                            "/", "/favicon.ico", "/error",
                            "/*.html", "/*.js", "/*.css", "/*.png", "/*.jpg", "/*.svg",
                            "/index.html", "/login.html", "/my-tickets.html",
                            "/trips.html", "/seat-selection.html", "/payment.html",
                            "/operator.html", "/admin.html", "/api.js"
                        ).permitAll()

                        // Public endpoints của ứng dụng
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/trips/**").permitAll()
                        .requestMatchers("/api/v1/payments/webhook").permitAll()
                        .requestMatchers("/api/v1/payments/qr/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/tickets/hold").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/tickets/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/tickets/cancel/**").permitAll()

                        // Role CUSTOMER / Authenticated for tickets & payments
                        .requestMatchers("/api/v1/tickets/**").hasAnyRole("CUSTOMER", "OPERATOR", "ADMIN")
                        .requestMatchers("/api/v1/payments/**").hasAnyRole("CUSTOMER", "OPERATOR", "ADMIN")

                        // Role OPERATOR
                        .requestMatchers("/api/v1/operator/**").hasAnyRole("OPERATOR", "ADMIN")

                        // Role ADMIN
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // Tất cả các request còn lại đều bắt buộc xác thực
                        .anyRequest().authenticated()
                )

                // 6. Cấu hình AuthenticationProvider và thêm JwtFilter trước UsernamePasswordAuthenticationFilter
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Cấu hình CorsConfigurationSource chuẩn cho Spring Security 6.
     * Bean này được Spring Security tích hợp trực tiếp vào Filter Chain (CorsFilter),
     * giúp xử lý các Header CORS (Access-Control-Allow-Origin, Access-Control-Allow-Methods, ...)
     * và phản hồi Preflight OPTIONS với mã HTTP 200 OK ngay tại tầng Security Filter.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 1. Cho phép tất cả Origins thông qua allowedOriginPatterns (hỗ trợ Cloudflare Tunnel, Vercel, Netlify, Localhost)
        configuration.setAllowedOriginPatterns(List.of("*"));

        // 2. Cho phép các HTTP Methods theo chuẩn RESTful
        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // 3. Cho phép các Headers cần thiết khi gửi kèm JWT Bearer Token hoặc JSON Payload
        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin"
        ));

        // 4. Cho phép Client đọc được Header Authorization trả về từ Server
        configuration.setExposedHeaders(List.of("Authorization"));

        // 5. Bắt buộc bật true nếu client cần gửi kèm Cookie hoặc Authorization Header
        configuration.setAllowCredentials(true);

        // 6. Thời gian trình duyệt được phép cache kết quả Preflight request (3600 giây = 1 giờ)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Áp dụng cấu hình CORS cho toàn bộ mọi đường dẫn trong hệ thống
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
