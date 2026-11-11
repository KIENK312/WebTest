package com.bus.booking.dto.request;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripSearchRequest {

    // Tùy chọn: Nếu để trống, hệ thống sẽ trả về tất cả chuyến xe đang mở bán
    private String departurePoint;
    private String arrivalPoint;
    private String operatorCode;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate departureDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endTime;

    public String getOrigin() {
        return departurePoint;
    }

    public void setOrigin(String origin) {
        this.departurePoint = origin;
    }

    public String getDestination() {
        return arrivalPoint;
    }

    public void setDestination(String destination) {
        this.arrivalPoint = destination;
    }
}
