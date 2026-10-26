package com.bus.booking.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyRevenueDto {
    private String month;       // VD: "T1", "T2", ... hoặc "T10/26"
    private int monthNumber;    // 1 -> 12
    private int year;
    private BigDecimal revenue;
    private long ticketCount;
}
