package com.bus.booking.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentWebhookRequest {

    private String gateway; // VD: "VIETQR", "SEPAY", "CASSO", "MBBANK"

    @NotBlank(message = "Mã giao dịch ngân hàng (transactionCode) không được để trống")
    private String transactionCode;

    @NotBlank(message = "Nội dung chuyển khoản (transferContent) không được để trống")
    private String transferContent; // Chứa nội dung bắt buộc: VE{ticketId}

    @NotNull(message = "Số tiền chuyển khoản (transferAmount) không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Số tiền chuyển khoản phải lớn hơn 0")
    private BigDecimal transferAmount;

    private String bankAccount;

    private LocalDateTime transactionDate;
}
