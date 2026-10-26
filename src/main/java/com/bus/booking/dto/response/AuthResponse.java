package com.bus.booking.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String accessToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private long expiresIn;
    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private String role;
    private Long operatorId;
}
