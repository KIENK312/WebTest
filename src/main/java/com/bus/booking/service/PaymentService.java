package com.bus.booking.service;

import com.bus.booking.dto.request.PaymentWebhookRequest;
import com.bus.booking.dto.response.PaymentQrResponse;
import com.bus.booking.dto.response.PaymentWebhookResponse;

public interface PaymentService {

    /**
     * Sinh thông tin mã QR VietQR thanh toán cho vé xe với nội dung bắt buộc: VE{ticketId}.
     */
    PaymentQrResponse createPaymentQr(Long ticketId);

    /**
     * Xử lý webhook callback từ Ngân hàng / Cổng thanh toán:
     * - Kiểm tra tính bất biến (Idempotency check).
     * - Trích xuất ticketId từ nội dung chuyển khoản.
     * - Đối soát số tiền thanh toán.
     * - Cập nhật trạng thái vé và kích hoạt gửi email bất đồng bộ.
     */
    PaymentWebhookResponse processWebhook(PaymentWebhookRequest payload);
}
