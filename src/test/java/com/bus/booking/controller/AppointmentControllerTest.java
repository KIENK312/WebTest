package com.bus.booking.controller;

import com.bus.booking.dto.request.AppointmentBookingRequest;
import com.bus.booking.dto.response.AppointmentResponseDto;
import com.bus.booking.enums.AppointmentStatus;
import com.bus.booking.service.AppointmentService;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppointmentService appointmentService;

    @Test
    @DisplayName("POST /api/v1/appointments/schedule: Đăng ký lịch hẹn thành công -> 201 Created")
    void testScheduleAppointment_Returns201Created() throws Exception {
        AppointmentBookingRequest request = AppointmentBookingRequest.builder()
                .customerName("Nguyễn Trung Kiên")
                .customerPhone("0901234567")
                .customerEmail("kien@example.com")
                .appointmentDate(LocalDate.now().plusDays(1))
                .timeSlot("09:15 - 10:15")
                .consultationType("Tư vấn lộ trình")
                .build();

        AppointmentResponseDto mockResponse = AppointmentResponseDto.builder()
                .id(1L)
                .appointmentCode("HEN-20261112-DEMO")
                .customerName("Nguyễn Trung Kiên")
                .customerPhone("0901234567")
                .appointmentDate(request.getAppointmentDate())
                .timeSlot("09:15 - 10:15")
                .status(AppointmentStatus.PENDING)
                .statusDescription("Chờ tiếp nhận")
                .build();

        when(appointmentService.scheduleAppointment(any(AppointmentBookingRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/appointments/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.appointmentCode").value("HEN-20261112-DEMO"))
                .andExpect(jsonPath("$.data.customerName").value("Nguyễn Trung Kiên"));
    }

    @Test
    @DisplayName("GET /api/v1/appointments/{code}: Tra cứu chi tiết lịch hẹn -> 200 OK")
    void testGetAppointmentByCode_Returns200() throws Exception {
        AppointmentResponseDto mockResponse = AppointmentResponseDto.builder()
                .id(1L)
                .appointmentCode("HEN-TEST-CODE")
                .customerName("Trần Văn A")
                .customerPhone("0988776655")
                .status(AppointmentStatus.CONFIRMED)
                .build();

        when(appointmentService.getAppointmentByCode(eq("HEN-TEST-CODE"))).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/appointments/HEN-TEST-CODE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.appointmentCode").value("HEN-TEST-CODE"));
    }

    @Test
    @WithMockUser(username = "operator_futa", roles = {"OPERATOR"})
    @DisplayName("PUT /api/v1/appointments/{id}/status: Nhân viên cập nhật trạng thái -> 200 OK")
    void testUpdateStatus_Returns200() throws Exception {
        AppointmentResponseDto mockResponse = AppointmentResponseDto.builder()
                .id(1L)
                .appointmentCode("HEN-TEST-01")
                .status(AppointmentStatus.CONFIRMED)
                .statusDescription("Đã xác nhận lịch")
                .assignedStaff("Trần Minh Tuấn")
                .build();

        when(appointmentService.updateStatus(eq(1L), eq(AppointmentStatus.CONFIRMED), any())).thenReturn(mockResponse);

        mockMvc.perform(put("/api/v1/appointments/1/status")
                        .param("status", "CONFIRMED")
                        .param("assignedStaff", "Trần Minh Tuấn"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
    }
}
