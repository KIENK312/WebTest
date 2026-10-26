package com.bus.booking.controller;

import com.bus.booking.dto.request.ScheduleMaintenanceRequest;
import com.bus.booking.dto.response.ScheduleMaintenanceResponse;
import com.bus.booking.enums.MaintenanceStatus;
import com.bus.booking.service.MaintenanceService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MaintenanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MaintenanceService maintenanceService;

    @Test
    @WithMockUser(username = "operator1", roles = {"OPERATOR"})
    @DisplayName("POST /api/v1/operator/maintenance: Lên lịch bảo trì thành công -> Trả về 201 Created bọc trong ApiResponse")
    void testScheduleMaintenance_Returns201Created() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(5);
        LocalDateTime end = start.plusDays(2);

        ScheduleMaintenanceRequest request = ScheduleMaintenanceRequest.builder()
                .busId(10L)
                .startTime(start)
                .endTime(end)
                .maintenanceType("PERIODIC")
                .cost(new BigDecimal("2000000.00"))
                .description("Bảo dưỡng định kỳ")
                .build();

        ScheduleMaintenanceResponse response = ScheduleMaintenanceResponse.builder()
                .maintenanceLogId(1L)
                .busId(10L)
                .plateNumber("51B-123.45")
                .startTime(start)
                .endTime(end)
                .maintenanceType("PERIODIC")
                .status(MaintenanceStatus.IN_PROGRESS)
                .message("Lên lịch bảo trì xe thành công")
                .build();

        when(maintenanceService.scheduleMaintenance(any(ScheduleMaintenanceRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/operator/maintenance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.maintenanceLogId").value(1))
                .andExpect(jsonPath("$.data.busId").value(10))
                .andExpect(jsonPath("$.data.plateNumber").value("51B-123.45"));
    }
}
