package com.bus.booking.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleMaintenanceRequest {

    @NotNull(message = "Mã xe (busId) không được để trống")
    private Long busId;

    @NotNull(message = "Thời gian bắt đầu bảo dưỡng không được để trống")
    private LocalDateTime startTime;

    @NotNull(message = "Thời gian dự kiến hoàn thành không được để trống")
    private LocalDateTime endTime;

    @Builder.Default
    private String maintenanceType = "PERIODIC";

    @DecimalMin(value = "0.0", inclusive = true, message = "Chi phí bảo dưỡng phải lớn hơn hoặc bằng 0")
    @Builder.Default
    private BigDecimal cost = BigDecimal.ZERO;

    private String description;

    public void setNotes(String notes) {
        if (this.description == null) {
            this.description = notes;
        }
    }

    public String getMaintenanceType() {
        return (maintenanceType == null || maintenanceType.isBlank()) ? "PERIODIC" : maintenanceType;
    }
}
