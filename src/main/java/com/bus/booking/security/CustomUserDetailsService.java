package com.bus.booking.security;

import com.bus.booking.entity.UserEntity;
import com.bus.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        log.debug("Đang tải thông tin xác thực cho: {}", usernameOrEmail);

        // Cho phép người dùng đăng nhập bằng Username hoặc Email
        UserEntity user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> {
                    log.warn("Không tìm thấy tài khoản với định danh: {}", usernameOrEmail);
                    return new UsernameNotFoundException("Không tìm thấy người dùng với username hoặc email: " + usernameOrEmail);
                });

        return new CustomUserDetails(user);
    }
}
