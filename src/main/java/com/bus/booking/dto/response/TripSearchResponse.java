package com.bus.booking.dto.response;

import com.bus.booking.enums.TripStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripSearchResponse {

    private Long tripId;
    private Long routeId;
    private String origin;
    private String destination;

    private Long operatorId;
    private String operatorName;
    private String operatorCode;

    private Long busId;
    private String busPlateNumber;
    private String busType;

    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private BigDecimal basePrice;
    private TripStatus status;

    private int totalSeats;
    private int availableSeats;
}
