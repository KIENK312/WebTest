package com.bus.booking.service;

import com.bus.booking.dto.request.PaymentWebhookRequest;
import com.bus.booking.dto.response.PaymentQrResponse;
import com.bus.booking.dto.response.PaymentWebhookResponse;
import com.bus.booking.entity.PaymentEntity;
import com.bus.booking.entity.TicketEntity;
import com.bus.booking.enums.PaymentStatus;
import com.bus.booking.enums.TicketStatus;
import com.bus.booking.repository.PaymentRepository;
import com.bus.booking.repository.TicketRepository;
import com.bus.booking.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private TicketEntity ticket;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "bankId", "970422");
        ReflectionTestUtils.setField(paymentService, "bankName", "MBBank");
        ReflectionTestUtils.setField(paymentService, "accountNumber", "090123456789");
        ReflectionTestUtils.setField(paymentService, "accountName", "HE THONG BAN VE XE KHACH");
        ReflectionTestUtils.setField(paymentService, "template", "compact2");

        ticket = TicketEntity.builder()
                .id(100L)
                .ticketCode("TK-100-XYZ")
                .customerName("Nguyễn Văn An")
                .customerEmail("an.nguyen@example.com")
                .customerPhone("0909123456")
                .price(new BigDecimal("350000.00"))
                .status(TicketStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("createPaymentQr: Sinh mã VietQR với nội dung bắt buộc VE{ticketId}")
    void testCreatePaymentQr_Success() {
        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(paymentRepository.findByTicketIdAndStatus(100L, PaymentStatus.PENDING))
                .thenReturn(Optional.empty());

        PaymentEntity savedPayment = PaymentEntity.builder()
                .id(1L)
                .ticket(ticket)
                .amount(ticket.getPrice())
                .paymentMethod("VIETQR")
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(savedPayment);

        PaymentQrResponse response = paymentService.createPaymentQr(100L);

        assertNotNull(response);
        assertEquals("VE100", response.getTransferContent(), "Nội dung chuyển khoản phải là VE{ticketId}");
        assertEquals(new BigDecimal("350000.00"), response.getAmount());
        assertTrue(response.getQrCodeUrl().contains("img.vietqr.io"));
        assertTrue(response.getQrCodeUrl().contains("VE100"));
        assertEquals(PaymentStatus.PENDING, response.getStatus());
    }

    @Test
    @DisplayName("processWebhook: Webhook hợp lệ -> Cập nhật Ticket CONFIRMED, Payment SUCCESS và gửi Email @Async")
    void testProcessWebhook_Success() {
        PaymentWebhookRequest payload = PaymentWebhookRequest.builder()
                .transactionCode("FT26102199887766")
                .transferContent("Chuyen tien mua ve VE100")
                .transferAmount(new BigDecimal("350000.00"))
                .transactionDate(LocalDateTime.now())
                .build();

        when(paymentRepository.existsByTransactionCode("FT26102199887766")).thenReturn(false);
        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(TicketEntity.class))).thenAnswer(i -> i.getArgument(0));

        PaymentEntity pendingPayment = PaymentEntity.builder()
                .id(5L)
                .ticket(ticket)
                .amount(ticket.getPrice())
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentRepository.findByTicketIdAndStatus(100L, PaymentStatus.PENDING))
                .thenReturn(Optional.of(pendingPayment));
        when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(i -> i.getArgument(0));

        PaymentWebhookResponse response = paymentService.processWebhook(payload);

        assertTrue(response.isSuccess());
        assertEquals(100L, response.getTicketId());
        assertEquals("FT26102199887766", response.getTransactionCode());
        assertEquals(TicketStatus.CONFIRMED, ticket.getStatus(), "Trạng thái vé phải được chuyển thành CONFIRMED");

        // Xác thực gửi email bất đồng bộ được gọi
        verify(notificationService, times(1)).sendBookingConfirmationEmail(eq(ticket));
    }

    @Test
    @DisplayName("processWebhook: Idempotency Check -> Giao dịch đã tồn tại thì bỏ qua không xử lý lại")
    void testProcessWebhook_Idempotency() {
        PaymentWebhookRequest payload = PaymentWebhookRequest.builder()
                .transactionCode("FT26102199887766")
                .transferContent("VE100")
                .transferAmount(new BigDecimal("350000.00"))
                .build();

        // Giả lập giao dịch này ĐÃ ĐƯỢC XỬ LÝ trước đó
        when(paymentRepository.existsByTransactionCode("FT26102199887766")).thenReturn(true);

        PaymentWebhookResponse response = paymentService.processWebhook(payload);

        assertTrue(response.isSuccess());
        assertTrue(response.getMessage().contains("Idempotent"));

        // Tuyệt đối không lưu lại vé hay gửi email lần 2
        verify(ticketRepository, never()).save(any());
        verify(notificationService, never()).sendBookingConfirmationEmail(any());
    }

    @Test
    @DisplayName("processWebhook: Đối soát số tiền thiếu -> Báo lỗi IllegalArgumentException")
    void testProcessWebhook_AmountMismatch() {
        PaymentWebhookRequest payload = PaymentWebhookRequest.builder()
                .transactionCode("FT_FAIL_01")
                .transferContent("VE100")
                .transferAmount(new BigDecimal("200000.00")) // Giá vé 350.000 nhưng chỉ chuyển 200.000
                .build();

        when(paymentRepository.existsByTransactionCode("FT_FAIL_01")).thenReturn(false);
        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.processWebhook(payload)
        );

        assertTrue(ex.getMessage().contains("không đủ so với giá vé"));
        verify(notificationService, never()).sendBookingConfirmationEmail(any());
    }
}
