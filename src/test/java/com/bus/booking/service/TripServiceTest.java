package com.bus.booking.service;

import com.bus.booking.dto.request.TripSearchRequest;
import com.bus.booking.dto.response.TripSearchResponse;
import com.bus.booking.entity.BusEntity;
import com.bus.booking.entity.BusOperatorEntity;
import com.bus.booking.entity.RouteEntity;
import com.bus.booking.entity.TripEntity;
import com.bus.booking.enums.BusStatus;
import com.bus.booking.enums.TripStatus;
import com.bus.booking.repository.MaintenanceLogRepository;
import com.bus.booking.repository.TicketRepository;
import com.bus.booking.repository.TripRepository;
import com.bus.booking.service.impl.TripServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private MaintenanceLogRepository maintenanceLogRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private com.bus.booking.repository.SeatRepository seatRepository;

    @InjectMocks
    private TripServiceImpl tripService;

    private TripEntity activeTrip;
    private TripEntity maintenanceTrip;
    private BusEntity bus1;
    private BusEntity bus2;

    @BeforeEach
    void setUp() {
        BusOperatorEntity operator = BusOperatorEntity.builder()
                .id(1L)
                .name("Phương Trang")
                .code("FUTA")
                .contactPhone("19006067")
                .build();

        RouteEntity route = RouteEntity.builder()
                .id(1L)
                .operator(operator)
                .origin("Hà Nội")
                .destination("Đà Nẵng")
                .build();

        bus1 = BusEntity.builder()
                .id(101L)
                .operator(operator)
                .plateNumber("29B-111.11")
                .busType("LIMOUSINE")
                .totalSeats(34)
                .status(BusStatus.ACTIVE)
                .build();

        bus2 = BusEntity.builder()
                .id(102L)
                .operator(operator)
                .plateNumber("29B-222.22")
                .busType("SLEEPER")
                .totalSeats(40)
                .status(BusStatus.ACTIVE)
                .build();

        LocalDateTime depTime = LocalDateTime.now().plusDays(1);
        LocalDateTime arrTime = depTime.plusHours(12);

        activeTrip = TripEntity.builder()
                .id(1L)
                .route(route)
                .bus(bus1)
                .departureTime(depTime)
                .arrivalTime(arrTime)
                .basePrice(new BigDecimal("450000.00"))
                .status(TripStatus.SCHEDULED)
                .build();

        maintenanceTrip = TripEntity.builder()
                .id(2L)
                .route(route)
                .bus(bus2)
                .departureTime(depTime)
                .arrivalTime(arrTime)
                .basePrice(new BigDecimal("400000.00"))
                .status(TripStatus.SCHEDULED)
                .build();
    }

    @Test
    @DisplayName("searchTrips: Tự động lọc bỏ chuyến xe có phương tiện dính lịch bảo trì")
    void testSearchTrips_FiltersOutBusesInMaintenance() {
        TripSearchRequest request = TripSearchRequest.builder()
                .departurePoint("Hà Nội")
                .arrivalPoint("Đà Nẵng")
                .departureDate(LocalDate.now().plusDays(1))
                .build();

        when(tripRepository.findAvailableTrips(eq("Hà Nội"), eq("Đà Nẵng"), any(), any()))
                .thenReturn(List.of(activeTrip, maintenanceTrip));

        // bus1 không bảo trì, bus2 đang dính lịch bảo dưỡng
        when(maintenanceLogRepository.hasOverlappingMaintenance(eq(101L), any(), any()))
                .thenReturn(false);
        when(maintenanceLogRepository.hasOverlappingMaintenance(eq(102L), any(), any()))
                .thenReturn(true);

        when(ticketRepository.countByTripIdAndStatusIn(eq(1L), anyList()))
                .thenReturn(4L); // Đã đặt 4 vé

        List<TripSearchResponse> responses = tripService.searchTrips(request);

        assertNotNull(responses);
        assertEquals(1, responses.size(), "Chỉ chuyến có xe không dính bảo dưỡng mới được trả về");
        TripSearchResponse result = responses.get(0);
        assertEquals(1L, result.getTripId());
        assertEquals("29B-111.11", result.getBusPlateNumber());
        assertEquals(34, result.getTotalSeats());
        assertEquals(30, result.getAvailableSeats(), "34 ghế - 4 ghế đã đặt = 30 ghế trống");
    }
}
