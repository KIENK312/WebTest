package com.bus.booking.controller;

import com.bus.booking.dto.request.HoldSeatRequest;
import com.bus.booking.dto.response.HoldSeatResponse;
import com.bus.booking.enums.TicketStatus;
import com.bus.booking.service.BookingService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @Test
    @WithMockUser(username = "customer1", roles = {"CUSTOMER"})
    @DisplayName("POST /api/v1/tickets/hold: Đặt giữ chỗ thành công -> Trả về 201 Created bọc trong ApiResponse")
    void testHoldSeat_Returns201Created() throws Exception {
        HoldSeatRequest request = HoldSeatRequest.builder()
                .tripId(1L)
                .seatId(20L)
                .customerName("Nguyễn Văn A")
                .customerPhone("0901234567")
                .build();

        HoldSeatResponse response = HoldSeatResponse.builder()
                .ticketId(10L)
                .ticketCode("TK-T1-A01-123456")
                .tripId(1L)
                .seatId(20L)
                .seatNumber("A01")
                .customerName("Nguyễn Văn A")
                .customerPhone("0901234567")
                .price(new BigDecimal("350000.00"))
                .status(TicketStatus.PENDING)
                .bookingTime(LocalDateTime.now())
                .holdExpiresAt(LocalDateTime.now().plusMinutes(15))
                .message("Giữ chỗ thành công trong 15 phút")
                .build();

        when(bookingService.holdSeat(any(HoldSeatRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/tickets/hold")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Giữ chỗ thành công trong 15 phút"))
                .andExpect(jsonPath("$.data.ticketCode").value("TK-T1-A01-123456"))
                .andExpect(jsonPath("$.data.seatNumber").value("A01"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "customer1", roles = {"CUSTOMER"})
    @DisplayName("PUT /api/v1/tickets/cancel/{id}: Hủy vé thành công -> Trả về 200 OK bọc trong ApiResponse")
    void testCancelTicket_Returns200Ok() throws Exception {
        HoldSeatResponse response = HoldSeatResponse.builder()
                .ticketId(10L)
                .ticketCode("TK-T1-A01-123456")
                .seatNumber("A01")
                .status(TicketStatus.CANCELLED)
                .message("Đã hủy vé thành công")
                .build();

        when(bookingService.cancelTicket(eq(10L))).thenReturn(response);

        mockMvc.perform(put("/api/v1/tickets/cancel/10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }
}
