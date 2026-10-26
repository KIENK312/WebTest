package com.bus.booking.dto.response;

import com.bus.booking.enums.OperatorStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerDto {
    private Long id;
    private String name;
    private String code;
    private String contactEmail;
    private String contactPhone;
    private String address;
    private OperatorStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
