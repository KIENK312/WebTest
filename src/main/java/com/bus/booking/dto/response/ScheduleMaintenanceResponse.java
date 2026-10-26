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
public class ScheduleMaintenanceResponse {

    private Long maintenanceLogId;
    private Long busId;
    private String plateNumber;
    private String busType;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String maintenanceType;
    private BigDecimal cost;
    private String description;
    private MaintenanceStatus status;

    private String message;

    public Long getId() {
        return maintenanceLogId;
    }

    public String getNotes() {
        return description;
    }
}
