package com.bus.booking.dto.response;

import com.bus.booking.enums.MaintenanceStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceDto {
    private Long id;
    private Long busId;
    private String busPlateNumber;
    private String busType;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String maintenanceType;
    private BigDecimal cost;
    private String description;
    private MaintenanceStatus status;
    private LocalDateTime createdAt;
}
