package com.bus.booking.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteDto {
    private Long id;
    private Long operatorId;
    private String operatorName;
    private String operatorCode;
    private String origin;
    private String destination;
    private BigDecimal distanceKm;
    private BigDecimal estimatedDurationHours;
    private String status;
    private LocalDateTime createdAt;

    public String getDeparturePoint() {
        return origin;
    }

    public String getArrivalPoint() {
        return destination;
    }
}
