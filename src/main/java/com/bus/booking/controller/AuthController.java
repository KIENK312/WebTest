package com.bus.booking.controller;

import com.bus.booking.dto.request.LoginRequest;
import com.bus.booking.dto.request.RegisterRequest;
import com.bus.booking.dto.response.ApiResponse;
import com.bus.booking.dto.response.AuthResponse;
import com.bus.booking.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse servletResponse) {
        AuthResponse response = authService.login(request);

        // Lưu JWT vào HttpOnly Cookie để các request từ Thymeleaf / HTML Form tự động mang theo
        Cookie jwtCookie = new Cookie("jwt_token", response.getAccessToken());
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false); // Cho phép HTTP trên localhost development
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(7 * 24 * 60 * 60); // 7 ngày
        servletResponse.addCookie(jwtCookie);

        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse servletResponse) {
        Cookie jwtCookie = new Cookie("jwt_token", "");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        servletResponse.addCookie(jwtCookie);

        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công", null));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse servletResponse) {
        AuthResponse response = authService.register(request);

        // Tự động set cookie sau khi đăng ký thành công
        Cookie jwtCookie = new Cookie("jwt_token", response.getAccessToken());
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(7 * 24 * 60 * 60);
        servletResponse.addCookie(jwtCookie);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Đăng ký tài khoản thành công", response));
    }
}
