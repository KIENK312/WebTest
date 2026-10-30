package com.bus.booking.controller;

import com.bus.booking.dto.request.UpdateProfileRequest;
import com.bus.booking.dto.response.ApiResponse;
import com.bus.booking.dto.response.AuthResponse;
import com.bus.booking.security.CustomUserDetails;
import com.bus.booking.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Lấy thông tin hồ sơ của người dùng đang đăng nhập
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<AuthResponse>> getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("Vui lòng đăng nhập để xem thông tin cá nhân!");
        }
        AuthResponse response = userService.getProfile(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin tài khoản thành công", response));
    }

    /**
     * Chỉnh sửa thông tin tài khoản người dùng (Họ tên, Email, Số điện thoại, Mật khẩu mới)
     * Đảm bảo email và số điện thoại không trùng với người khác trong hệ thống.
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<AuthResponse>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        if (userDetails == null) {
            throw new IllegalArgumentException("Vui lòng đăng nhập để thực hiện thay đổi thông tin!");
        }
        AuthResponse response = userService.updateProfile(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin tài khoản thành công", response));
    }
}
