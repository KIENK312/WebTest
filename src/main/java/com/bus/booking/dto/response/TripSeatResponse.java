package com.bus.booking.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripSeatResponse {

    private Long seatId;
    private String seatNumber;
    private String seatType;
    private int floorNumber;
    private BigDecimal price;
    private String status; // "AVAILABLE", "HELD", "BOOKED"
}
