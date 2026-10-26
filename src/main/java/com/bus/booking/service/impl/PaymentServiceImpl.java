package com.bus.booking.service.impl;

import com.bus.booking.dto.request.PaymentWebhookRequest;
import com.bus.booking.dto.response.PaymentQrResponse;
import com.bus.booking.dto.response.PaymentWebhookResponse;
import com.bus.booking.entity.PaymentEntity;
import com.bus.booking.entity.TicketEntity;
import com.bus.booking.enums.PaymentStatus;
import com.bus.booking.enums.TicketStatus;
import com.bus.booking.exception.ResourceNotFoundException;
import com.bus.booking.repository.PaymentRepository;
import com.bus.booking.repository.TicketRepository;
import com.bus.booking.service.NotificationService;
import com.bus.booking.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final TicketRepository ticketRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;

    @Value("${vietqr.bank-id:970422}")
    private String bankId;

    @Value("${vietqr.bank-name:MBBank}")
    private String bankName;

    @Value("${vietqr.account-number:090123456789}")
    private String accountNumber;

    @Value("${vietqr.account-name:HE THONG BAN VE XE KHACH}")
    private String accountName;

    @Value("${vietqr.template:compact2}")
    private String template;

    // Pattern trích xuất ticketId từ nội dung chuyển khoản dạng VE{ticketId} (VD: VE100, VE 100, chuyen tien VE100)
    private static final Pattern TICKET_ID_PATTERN = Pattern.compile("(?i)VE\\s*(\\d+)");

    @Override
    @Transactional
    public PaymentQrResponse createPaymentQr(Long ticketId) {
        log.info("Yêu cầu sinh mã QR VietQR cho vé ID: {}", ticketId);

        // 1. Kiểm tra tồn tại của vé
        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vé xe với ID: " + ticketId));

        if (ticket.getStatus() == TicketStatus.CONFIRMED) {
            throw new IllegalStateException("Vé này đã được thanh toán thành công trước đó!");
        }

        if (ticket.getStatus() == TicketStatus.CANCELLED || ticket.getStatus() == TicketStatus.EXPIRED) {
            throw new IllegalStateException("Vé này đã bị hủy hoặc hết hạn, không thể thanh toán!");
        }

        // 2. Nội dung chuyển khoản bắt buộc: VE{ticketId}
        String transferContent = "VE" + ticket.getId();

        // 3. Tìm hoặc khởi tạo bản ghi Payment ở trạng thái PENDING
        PaymentEntity payment = paymentRepository.findByTicketIdAndStatus(ticket.getId(), PaymentStatus.PENDING)
                .orElseGet(() -> PaymentEntity.builder()
                        .ticket(ticket)
                        .amount(ticket.getPrice())
                        .paymentMethod("VIETQR")
                        .status(PaymentStatus.PENDING)
                        .build());

        payment.setAmount(ticket.getPrice());
        PaymentEntity savedPayment = paymentRepository.save(payment);

        // 4. Sinh URL ảnh QR code chuẩn VietQR
        String addInfoEncoded = URLEncoder.encode(transferContent, StandardCharsets.UTF_8);
        String accountNameEncoded = URLEncoder.encode(accountName, StandardCharsets.UTF_8);
        long amountInt = ticket.getPrice().longValue();

        String qrCodeUrl = String.format(
                "https://img.vietqr.io/image/%s-%s-%s.png?amount=%d&addInfo=%s&accountName=%s",
                bankId, accountNumber, template, amountInt, addInfoEncoded, accountNameEncoded
        );

        log.info("Sinh mã QR VietQR thành công: paymentId={}, transferContent={}, amount={}",
                savedPayment.getId(), transferContent, amountInt);

        return PaymentQrResponse.builder()
                .paymentId(savedPayment.getId())
                .ticketId(ticket.getId())
                .ticketCode(ticket.getTicketCode())
                .amount(ticket.getPrice())
                .transferContent(transferContent)
                .qrCodeUrl(qrCodeUrl)
                .bankId(bankId)
                .bankName(bankName)
                .accountNumber(accountNumber)
                .accountName(accountName)
                .status(savedPayment.getStatus())
                .message("Vui lòng quét mã QR hoặc chuyển khoản đúng nội dung: " + transferContent)
                .build();
    }

    @Override
    @Transactional
    public PaymentWebhookResponse processWebhook(PaymentWebhookRequest payload) {
        log.info("[WEBHOOK-NHẬN] Giao dịch từ cổng/ngân hàng: txCode={}, content='{}', amount={}",
                payload.getTransactionCode(), payload.getTransferContent(), payload.getTransferAmount());

        // 1. Idempotency Check: Kiểm tra giao dịch đã được xử lý chưa để tránh tính trùng
        if (paymentRepository.existsByTransactionCode(payload.getTransactionCode().trim())) {
            log.warn("[IDEMPOTENCY] Giao dịch mã '{}' đã được ghi nhận trước đó. Bỏ qua xử lý trùng lặp!",
                    payload.getTransactionCode());
            return PaymentWebhookResponse.builder()
                    .success(true)
                    .message("Giao dịch đã được ghi nhận trước đó (Idempotent response)")
                    .transactionCode(payload.getTransactionCode())
                    .amount(payload.getTransferAmount())
                    .processedAt(LocalDateTime.now())
                    .build();
        }

        // 2. Trích xuất ticketId từ nội dung chuyển khoản
        Matcher matcher = TICKET_ID_PATTERN.matcher(payload.getTransferContent());
        if (!matcher.find()) {
            log.error("[WEBHOOK-LỖI] Nội dung chuyển khoản '{}' không chứa cú pháp 'VE{ticketId}'",
                    payload.getTransferContent());
            throw new IllegalArgumentException(String.format(
                    "Nội dung chuyển khoản '%s' không chứa định dạng hợp lệ 'VE{ticketId}'",
                    payload.getTransferContent()
            ));
        }

        Long ticketId = Long.parseLong(matcher.group(1));

        // 3. Tìm vé xe tương ứng
        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vé xe tương ứng với mã: " + ticketId));

        // Nếu vé đã được xác nhận từ trước
        if (ticket.getStatus() == TicketStatus.CONFIRMED) {
            log.info("[WEBHOOK] Vé id={} đã ở trạng thái CONFIRMED.", ticket.getId());
            return PaymentWebhookResponse.builder()
                    .success(true)
                    .message("Vé xe đã được xác nhận thanh toán trước đó.")
                    .ticketId(ticket.getId())
                    .ticketCode(ticket.getTicketCode())
                    .transactionCode(payload.getTransactionCode())
                    .amount(payload.getTransferAmount())
                    .processedAt(LocalDateTime.now())
                    .build();
        }

        // 4. Đối soát số tiền chuyển khoản với giá vé (Reconciliation)
        if (payload.getTransferAmount().compareTo(ticket.getPrice()) < 0) {
            log.error("[WEBHOOK-ĐỐI SOÁT] Số tiền chuyển ({}) nhỏ hơn giá vé quy định ({}) của vé id={}",
                    payload.getTransferAmount(), ticket.getPrice(), ticket.getId());
            throw new IllegalArgumentException(String.format(
                    "Số tiền thanh toán (%s VNĐ) không đủ so với giá vé (%s VNĐ) của vé ID %d",
                    payload.getTransferAmount(), ticket.getPrice(), ticket.getId()
            ));
        }

        // 5. Cập nhật trạng thái vé sang CONFIRMED
        ticket.setStatus(TicketStatus.CONFIRMED);
        TicketEntity updatedTicket = ticketRepository.save(ticket);
        log.info("[WEBHOOK] Đã cập nhật Ticket ID={} sang CONFIRMED", updatedTicket.getId());

        // 6. Cập nhật hoặc tạo mới bản ghi thanh toán sang SUCCESS
        PaymentEntity payment = paymentRepository.findByTicketIdAndStatus(ticket.getId(), PaymentStatus.PENDING)
                .orElseGet(() -> PaymentEntity.builder()
                        .ticket(updatedTicket)
                        .amount(payload.getTransferAmount())
                        .paymentMethod("VIETQR")
                        .build());

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionCode(payload.getTransactionCode().trim());
        payment.setAmount(payload.getTransferAmount());
        payment.setPaymentTime(payload.getTransactionDate() != null ? payload.getTransactionDate() : LocalDateTime.now());

        PaymentEntity savedPayment = paymentRepository.save(payment);
        log.info("[WEBHOOK] Đã lưu Payment ID={} trạng thái SUCCESS cho vé ID={}",
                savedPayment.getId(), updatedTicket.getId());

        // 7. Kích hoạt gửi email xác nhận bất đồng bộ (@Async)
        notificationService.sendBookingConfirmationEmail(updatedTicket);

        return PaymentWebhookResponse.builder()
                .success(true)
                .message("Xử lý thanh toán webhook thành công. Vé đã được xác nhận.")
                .paymentId(savedPayment.getId())
                .ticketId(updatedTicket.getId())
                .ticketCode(updatedTicket.getTicketCode())
                .transactionCode(savedPayment.getTransactionCode())
                .amount(savedPayment.getAmount())
                .processedAt(LocalDateTime.now())
                .build();
    }
}
