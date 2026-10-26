package com.bus.booking.service.impl;

import com.bus.booking.dto.request.LoginRequest;
import com.bus.booking.dto.request.RegisterRequest;
import com.bus.booking.dto.response.AuthResponse;
import com.bus.booking.entity.BusOperatorEntity;
import com.bus.booking.entity.RoleEntity;
import com.bus.booking.entity.UserEntity;
import com.bus.booking.enums.UserStatus;
import com.bus.booking.exception.ResourceNotFoundException;
import com.bus.booking.repository.BusOperatorRepository;
import com.bus.booking.repository.RoleRepository;
import com.bus.booking.repository.UserRepository;
import com.bus.booking.security.CustomUserDetails;
import com.bus.booking.security.jwt.JwtTokenProvider;
import com.bus.booking.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BusOperatorRepository busOperatorRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Xử lý đăng nhập cho người dùng: {}", request.getUsernameOrEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail().trim(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String jwt = tokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .expiresIn(jwtExpirationMs / 1000)
                .userId(userDetails.getId())
                .username(userDetails.getUsername())
                .fullName(userDetails.getFullName())
                .email(userDetails.getEmail())
                .role(userDetails.getRoleName())
                .operatorId(userDetails.getOperatorId())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Xử lý đăng ký tài khoản mới: username={}, email={}",
                request.getUsername(), request.getEmail());

        if (userRepository.existsByUsername(request.getUsername().trim())) {
            throw new IllegalArgumentException("Tên đăng nhập '" + request.getUsername() + "' đã được sử dụng!");
        }

        if (userRepository.existsByEmail(request.getEmail().trim())) {
            throw new IllegalArgumentException("Email '" + request.getEmail() + "' đã được đăng ký tài khoản!");
        }

        // Mặc định vai trò là ROLE_CUSTOMER nếu không chỉ định
        String targetRole = request.getRoleName() != null ? request.getRoleName().trim() : "ROLE_CUSTOMER";
        RoleEntity role = roleRepository.findByName(targetRole)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò hệ thống: " + targetRole));

        // Kiểm tra đối tác nếu là tài khoản nhà xe
        BusOperatorEntity operator = null;
        if (request.getOperatorId() != null) {
            operator = busOperatorRepository.findById(request.getOperatorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà xe với ID: " + request.getOperatorId()));
        }

        UserEntity user = UserEntity.builder()
                .username(request.getUsername().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .email(request.getEmail().trim())
                .phoneNumber(request.getPhoneNumber().trim())
                .role(role)
                .operator(operator)
                .status(UserStatus.ACTIVE)
                .build();

        UserEntity savedUser = userRepository.save(user);
        CustomUserDetails userDetails = new CustomUserDetails(savedUser);
        String jwt = tokenProvider.generateTokenFromUserDetails(userDetails);

        return AuthResponse.builder()
                .accessToken(jwt)
                .tokenType("Bearer")
                .expiresIn(jwtExpirationMs / 1000)
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().getName())
                .operatorId(operator != null ? operator.getId() : null)
                .build();
    }
}
