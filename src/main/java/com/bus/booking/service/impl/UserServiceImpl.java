package com.bus.booking.service.impl;

import com.bus.booking.dto.request.UpdateProfileRequest;
import com.bus.booking.dto.response.AuthResponse;
import com.bus.booking.entity.UserEntity;
import com.bus.booking.exception.ResourceNotFoundException;
import com.bus.booking.repository.UserRepository;
import com.bus.booking.security.CustomUserDetails;
import com.bus.booking.security.jwt.JwtTokenProvider;
import com.bus.booking.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Value("${jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    @Override
    @Transactional(readOnly = true)
    public AuthResponse getProfile(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin người dùng với ID: " + userId));

        return toAuthResponse(user, null);
    }

    @Override
    @Transactional
    public AuthResponse updateProfile(Long userId, UpdateProfileRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin người dùng với ID: " + userId));

        String newEmail = request.getEmail().trim();
        String newPhone = request.getPhoneNumber().trim();
        String newFullName = request.getFullName().trim();

        // 1. Kiểm tra chống trùng Email với tài khoản khác
        userRepository.findByEmail(newEmail).ifPresent(existing -> {
            if (!existing.getId().equals(user.getId())) {
                throw new IllegalArgumentException("Email '" + newEmail + "' đã được sử dụng bởi một tài khoản khác!");
            }
        });

        // 2. Kiểm tra chống trùng Số điện thoại với tài khoản khác
        userRepository.findByPhoneNumber(newPhone).ifPresent(existing -> {
            if (!existing.getId().equals(user.getId())) {
                throw new IllegalArgumentException("Số điện thoại '" + newPhone + "' đã được sử dụng bởi một tài khoản khác!");
            }
        });

        // 3. Cập nhật thông tin cơ bản
        user.setFullName(newFullName);
        user.setEmail(newEmail);
        user.setPhoneNumber(newPhone);

        // 4. Cập nhật mật khẩu mới nếu người dùng nhập
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            String newPassword = request.getPassword().trim();
            if (newPassword.length() < 6) {
                throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự!");
            }
            user.setPassword(passwordEncoder.encode(newPassword));
            log.info("Người dùng '{}' vừa cập nhật mật khẩu mới", user.getUsername());
        }

        UserEntity saved = userRepository.save(user);
        log.info("Cập nhật thành công thông tin người dùng ID #{} (username: {})", saved.getId(), saved.getUsername());

        // 5. Sinh lại JWT Token mới mang thông tin mới nhất
        CustomUserDetails userDetails = new CustomUserDetails(saved);
        String newToken = tokenProvider.generateTokenFromUserDetails(userDetails);

        return toAuthResponse(saved, newToken);
    }

    private AuthResponse toAuthResponse(UserEntity user, String token) {
        CustomUserDetails details = new CustomUserDetails(user);
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType(token != null ? "Bearer" : null)
                .expiresIn(jwtExpirationMs / 1000)
                .userId(details.getId())
                .username(details.getUsername())
                .fullName(details.getFullName())
                .email(details.getEmail())
                .phone(details.getPhoneNumber())
                .phoneNumber(details.getPhoneNumber())
                .role(details.getRoleName())
                .operatorId(details.getOperatorId())
                .operatorName(details.getOperatorName())
                .operatorCode(details.getOperatorCode())
                .build();
    }
}
