package com.bus.booking.controller;

import com.bus.booking.dto.request.HoldSeatRequest;
import com.bus.booking.dto.response.HoldSeatResponse;
import com.bus.booking.dto.response.PaymentQrResponse;
import com.bus.booking.enums.TicketStatus;
import com.bus.booking.service.BookingService;
import com.bus.booking.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WebReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private PaymentService paymentService;

    @Test
    @DisplayName("[PRG Bước 1] POST /reservations/book: Giữ chỗ thành công -> Chuyển hướng Redirect sang payment-step")
    void testBookTicket_RedirectsToPaymentStep() throws Exception {
        HoldSeatResponse mockResponse = HoldSeatResponse.builder()
                .ticketId(100L)
                .ticketCode("TK-TEST-100")
                .seatNumber("A01")
                .price(new BigDecimal("350000.00"))
                .status(TicketStatus.PENDING)
                .build();

        when(bookingService.holdSeat(any(HoldSeatRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/reservations/book")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("tripId", "1")
                        .param("seatId", "20")
                        .param("customerName", "Nguyễn Văn A")
                        .param("customerPhone", "0901234567")
                        .param("customerEmail", "a@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservations/100/payment-step"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("[PRG Bước 2] GET /reservations/100/payment-step: Hiển thị giao diện thanh toán & VietQR")
    void testShowPaymentStep_RendersPaymentView() throws Exception {
        HoldSeatResponse mockTicket = HoldSeatResponse.builder()
                .ticketId(100L)
                .ticketCode("TK-TEST-100")
                .customerName("Nguyễn Văn A")
                .customerPhone("0901234567")
                .origin("Hà Nội")
                .destination("Đà Nẵng")
                .departureTime(LocalDateTime.now().plusDays(1))
                .seatNumber("A01")
                .price(new BigDecimal("350000.00"))
                .status(TicketStatus.PENDING)
                .build();

        PaymentQrResponse mockQr = PaymentQrResponse.builder()
                .paymentId(1L)
                .qrCodeUrl("https://img.vietqr.io/image/970422-090123456789-compact2.png")
                .transferContent("VE100")
                .amount(new BigDecimal("350000.00"))
                .build();

        when(bookingService.getTicketById(eq(100L))).thenReturn(mockTicket);
        when(paymentService.createPaymentQr(eq(100L))).thenReturn(mockQr);

        mockMvc.perform(get("/reservations/100/payment-step"))
                .andExpect(status().isOk())
                .andExpect(view().name("reservations/payment-step"))
                .andExpect(model().attributeExists("ticket"))
                .andExpect(model().attributeExists("qr"));
    }

    @Test
    @DisplayName("[PRG Bước 3 & 4] POST /reservations/100/pay: Thanh toán thành công -> Redirect sang confirmation")
    void testProcessPayment_RedirectsToConfirmation() throws Exception {
        HoldSeatResponse mockTicket = HoldSeatResponse.builder()
                .ticketId(100L)
                .price(new BigDecimal("350000.00"))
                .status(TicketStatus.PENDING)
                .build();

        when(bookingService.getTicketById(eq(100L))).thenReturn(mockTicket);

        mockMvc.perform(post("/reservations/100/pay"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservations/100/confirmation"))
                .andExpect(flash().attributeExists("paymentSuccess"));
    }

    @Test
    @DisplayName("[PRG Bước 4] GET /reservations/100/confirmation: Hiển thị Vé điện tử / Tem số")
    void testShowConfirmation_RendersConfirmationView() throws Exception {
        HoldSeatResponse mockTicket = HoldSeatResponse.builder()
                .ticketId(100L)
                .ticketCode("TK-CONFIRMED-100")
                .customerName("Nguyễn Văn A")
                .customerPhone("0901234567")
                .origin("Hà Nội")
                .destination("Đà Nẵng")
                .departureTime(LocalDateTime.now().plusDays(1))
                .seatNumber("A01")
                .price(new BigDecimal("350000.00"))
                .status(TicketStatus.CONFIRMED)
                .build();

        when(bookingService.getTicketById(eq(100L))).thenReturn(mockTicket);

        mockMvc.perform(get("/reservations/100/confirmation"))
                .andExpect(status().isOk())
                .andExpect(view().name("reservations/confirmation"))
                .andExpect(model().attributeExists("ticket"));
    }
}
