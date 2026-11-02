package com.bus.booking.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerLookupDto {

    private String customerName;
    private String customerPhone;
    private String customerEmail;

    private int totalTickets;
    private int confirmedTickets;
    private BigDecimal totalSpent;

    private List<HoldSeatResponse> tickets;
}
