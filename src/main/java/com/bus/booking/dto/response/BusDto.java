package com.bus.booking.dto.response;

import com.bus.booking.enums.BusStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusDto {
    private Long id;
    private Long operatorId;
    private String operatorName;
    private String operatorCode;
    private String plateNumber;
    private String busType;
    private Integer totalSeats;
    private BusStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getLicensePlate() {
        return plateNumber;
    }
}
