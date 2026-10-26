package com.bus.booking.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentWebhookResponse {

    private boolean success;
    private String message;
    private Long paymentId;
    private Long ticketId;
    private String ticketCode;
    private String transactionCode;
    private BigDecimal amount;
    private LocalDateTime processedAt;
}
