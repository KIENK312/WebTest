package com.bus.booking.controller;

import com.bus.booking.dto.request.PaymentWebhookRequest;
import com.bus.booking.dto.response.PaymentQrResponse;
import com.bus.booking.dto.response.PaymentWebhookResponse;
import com.bus.booking.enums.PaymentStatus;
import com.bus.booking.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @Test
    @WithMockUser(username = "customer1", roles = {"CUSTOMER"})
    @DisplayName("POST /api/v1/payments/qr/{ticketId}: Sinh mã VietQR cho vé thành công")
    void testCreatePaymentQr_Success() throws Exception {
        PaymentQrResponse response = PaymentQrResponse.builder()
                .paymentId(1L)
                .ticketId(100L)
                .ticketCode("TK-100-XYZ")
                .amount(new BigDecimal("350000.00"))
                .transferContent("VE100")
                .qrCodeUrl("https://img.vietqr.io/image/970422-090123456789-compact2.png")
                .bankId("970422")
                .bankName("MBBank")
                .accountNumber("090123456789")
                .accountName("HE THONG BAN VE XE KHACH")
                .status(PaymentStatus.PENDING)
                .message("Vui lòng chuyển khoản đúng nội dung VE100")
                .build();

        when(paymentService.createPaymentQr(eq(100L))).thenReturn(response);

        mockMvc.perform(post("/api/v1/payments/qr/100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.transferContent").value("VE100"))
                .andExpect(jsonPath("$.data.qrCodeUrl").exists())
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /api/v1/payments/webhook: Public endpoint, xử lý Webhook từ ngân hàng không cần JWT")
    void testHandleWebhook_PublicAccess_Success() throws Exception {
        PaymentWebhookRequest payload = PaymentWebhookRequest.builder()
                .transactionCode("TXN_MB_998877")
                .transferContent("VE100 chuyen tien")
                .transferAmount(new BigDecimal("350000.00"))
                .transactionDate(LocalDateTime.now())
                .build();

        PaymentWebhookResponse response = PaymentWebhookResponse.builder()
                .success(true)
                .message("Xử lý Webhook thành công")
                .ticketId(100L)
                .transactionCode("TXN_MB_998877")
                .amount(new BigDecimal("350000.00"))
                .processedAt(LocalDateTime.now())
                .build();

        when(paymentService.processWebhook(any(PaymentWebhookRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/payments/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.ticketId").value(100))
                .andExpect(jsonPath("$.data.transactionCode").value("TXN_MB_998877"));
    }
}
