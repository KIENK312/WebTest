package com.bus.booking.dto.response;

import com.bus.booking.enums.AppointmentStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentResponseDto {

    private Long id;
    private String appointmentCode;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private LocalDate appointmentDate;
    private String timeSlot;
    private String consultationType;
    private AppointmentStatus status;
    private String statusDescription;
    private String notes;
    private String assignedStaff;
    private LocalDateTime createdAt;
}
