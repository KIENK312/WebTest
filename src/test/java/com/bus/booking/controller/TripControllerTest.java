package com.bus.booking.controller;

import com.bus.booking.dto.response.TripSearchResponse;
import com.bus.booking.dto.response.TripSeatResponse;
import com.bus.booking.service.TripService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TripControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TripService tripService;

    @Test
    @DisplayName("GET /api/v1/trips/search: Công khai, trả về ApiResponse bọc danh sách chuyến xe")
    void testSearchTrips_ReturnsApiResponse() throws Exception {
        TripSearchResponse trip = TripSearchResponse.builder()
                .tripId(1L)
                .origin("Hà Nội")
                .destination("Đà Nẵng")
                .operatorName("Phương Trang")
                .busPlateNumber("29B-888.88")
                .departureTime(LocalDateTime.now().plusDays(1))
                .arrivalTime(LocalDateTime.now().plusDays(1).plusHours(12))
                .basePrice(new BigDecimal("450000.00"))
                .totalSeats(34)
                .availableSeats(30)
                .build();

        when(tripService.searchTrips(any())).thenReturn(List.of(trip));

        mockMvc.perform(get("/api/v1/trips/search")
                        .param("departurePoint", "Hà Nội")
                        .param("arrivalPoint", "Đà Nẵng")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data[0].tripId").value(1))
                .andExpect(jsonPath("$.data[0].origin").value("Hà Nội"))
                .andExpect(jsonPath("$.data[0].availableSeats").value(30))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("GET /api/v1/trips/{id}/seats: Công khai, trả về ApiResponse bọc sơ đồ ghế")
    void testGetTripSeats_ReturnsApiResponse() throws Exception {
        TripSeatResponse seat1 = TripSeatResponse.builder()
                .seatId(1L)
                .seatNumber("A01")
                .seatType("STANDARD")
                .floorNumber(1)
                .price(new BigDecimal("450000.00"))
                .status("AVAILABLE")
                .build();

        when(tripService.getTripSeats(1L)).thenReturn(List.of(seat1));

        mockMvc.perform(get("/api/v1/trips/1/seats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data[0].seatNumber").value("A01"))
                .andExpect(jsonPath("$.data[0].status").value("AVAILABLE"));
    }
}
