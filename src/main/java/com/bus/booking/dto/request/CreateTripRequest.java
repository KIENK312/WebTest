package com.bus.booking.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTripRequest {

    @NotNull(message = "Mã tuyến đường (routeId) không được để trống")
    private Long routeId;

    @NotNull(message = "Mã xe (busId) không được để trống")
    private Long busId;

    @NotNull(message = "Giờ xuất bến không được để trống")
    private LocalDateTime departureTime;

    @NotNull(message = "Giờ đến dự kiến không được để trống")
    private LocalDateTime arrivalTime;

    @NotNull(message = "Giá vé cơ sở không được để trống")
    @DecimalMin(value = "0.0", message = "Giá vé phải lớn hơn hoặc bằng 0")
    private BigDecimal basePrice;

    private String driverName;

    public void setPrice(BigDecimal price) {
        if (this.basePrice == null) {
            this.basePrice = price;
        }
    }
}
