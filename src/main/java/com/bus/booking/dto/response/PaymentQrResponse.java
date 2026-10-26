package com.bus.booking.dto.response;

import com.bus.booking.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentQrResponse {

    private Long paymentId;
    private Long ticketId;
    private String ticketCode;
    private BigDecimal amount;
    private String transferContent; // Bắt buộc định dạng: "VE{ticketId}"

    private String qrCodeUrl;
    private String bankId;
    private String bankName;
    private String accountNumber;
    private String accountName;

    private PaymentStatus status;
    private String message;
}
