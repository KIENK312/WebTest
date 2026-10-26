package com.bus.booking.dto.response;

import com.bus.booking.enums.TicketStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoldSeatResponse {

    private Long ticketId;
    private String ticketCode;
    private Long tripId;
    private Long seatId;
    private String seatNumber;

    private String customerName;
    private String customerPhone;
    private String customerEmail;

    private BigDecimal price;
    private TicketStatus status;
    private Long version;

    private LocalDateTime bookingTime;
    private LocalDateTime holdExpiresAt; // Thời hạn giữ chỗ (ví dụ: 15 phút sau bookingTime)
    private String message;
}
