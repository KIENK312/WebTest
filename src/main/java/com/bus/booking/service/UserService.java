package com.bus.booking.service;

import com.bus.booking.dto.request.UpdateProfileRequest;
import com.bus.booking.dto.response.AuthResponse;

public interface UserService {

    /**
     * Lấy thông tin hồ sơ của người dùng hiện tại
     */
    AuthResponse getProfile(Long userId);

    /**
     * Chỉnh sửa thông tin người dùng (Họ tên, Email, SĐT, Mật khẩu)
     * Đảm bảo email và số điện thoại không bị trùng với người dùng khác trong hệ thống.
     */
    AuthResponse updateProfile(Long userId, UpdateProfileRequest request);
}
