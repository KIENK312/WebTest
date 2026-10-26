package com.bus.booking.controller;

import com.bus.booking.dto.request.PaymentWebhookRequest;
import com.bus.booking.dto.response.ApiResponse;
import com.bus.booking.dto.response.PaymentQrResponse;
import com.bus.booking.dto.response.PaymentWebhookResponse;
import com.bus.booking.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Sinh thông tin mã QR VietQR thanh toán cho vé xe.
     * Nội dung chuyển khoản bắt buộc: VE{ticketId}.
     */
    @PostMapping("/qr/{ticketId}")
    public ResponseEntity<ApiResponse<PaymentQrResponse>> createPaymentQr(
            @PathVariable("ticketId") Long ticketId) {
        PaymentQrResponse response = paymentService.createPaymentQr(ticketId);
        return ResponseEntity.ok(ApiResponse.success("Sinh mã VietQR thành công", response));
    }

    /**
     * Webhook tiếp nhận callback thanh toán tự động từ Ngân hàng / Cổng thanh toán (SePay, Casso, etc.).
     * Thực hiện Idempotency Check, trích xuất mã vé, đối soát số tiền và gửi email thông báo bất đồng bộ.
     */
    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse<PaymentWebhookResponse>> handleWebhook(
            @Valid @RequestBody PaymentWebhookRequest payload) {
        PaymentWebhookResponse response = paymentService.processWebhook(payload);
        return ResponseEntity.ok(ApiResponse.success("Xử lý Webhook ngân hàng thành công", response));
    }
}
