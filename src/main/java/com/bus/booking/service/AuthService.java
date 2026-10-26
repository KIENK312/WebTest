package com.bus.booking.service;

import com.bus.booking.dto.request.LoginRequest;
import com.bus.booking.dto.request.RegisterRequest;
import com.bus.booking.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
}
