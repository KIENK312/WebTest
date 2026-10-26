package com.bus.booking.service;

import com.bus.booking.dto.request.ScheduleMaintenanceRequest;
import com.bus.booking.dto.response.ScheduleMaintenanceResponse;
import com.bus.booking.entity.BusEntity;
import com.bus.booking.entity.MaintenanceLogEntity;
import com.bus.booking.entity.TripEntity;
import com.bus.booking.enums.BusStatus;
import com.bus.booking.enums.MaintenanceStatus;
import com.bus.booking.enums.TripStatus;
import com.bus.booking.exception.BusInMaintenanceException;
import com.bus.booking.exception.ResourceNotFoundException;
import com.bus.booking.repository.BusRepository;
import com.bus.booking.repository.MaintenanceLogRepository;
import com.bus.booking.repository.TripRepository;
import com.bus.booking.service.impl.MaintenanceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceServiceTest {

    @Mock
    private BusRepository busRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private MaintenanceLogRepository maintenanceLogRepository;

    @InjectMocks
    private MaintenanceServiceImpl maintenanceService;

    private BusEntity bus;

    @BeforeEach
    void setUp() {
        bus = BusEntity.builder()
                .id(50L)
                .plateNumber("51B-888.88")
                .busType("LIMOUSINE")
                .totalSeats(34)
                .status(BusStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("scheduleMaintenance: Xung đột đệm 48h -> Hủy thao tác và ném BusInMaintenanceException")
    void testScheduleMaintenance_ThrowsWhenConflictingTripsIn48hBuffer() {
        LocalDateTime start = LocalDateTime.now().plusDays(5);
        LocalDateTime end = start.plusDays(2);

        ScheduleMaintenanceRequest request = ScheduleMaintenanceRequest.builder()
                .busId(50L)
                .startTime(start)
                .endTime(end)
                .maintenanceType("PERIODIC")
                .cost(new BigDecimal("5000000.00"))
                .description("Bảo dưỡng định kỳ 50,000 km")
                .build();

        when(busRepository.findById(50L)).thenReturn(Optional.of(bus));

        // Giả lập phát hiện 2 chuyến đi SCHEDULED trong khoảng [start - 48h, end + 48h]
        TripEntity trip1 = TripEntity.builder().id(201L).build();
        TripEntity trip2 = TripEntity.builder().id(202L).build();

        when(tripRepository.findConflictingTrips(eq(50L), eq(TripStatus.SCHEDULED), any(), any()))
                .thenReturn(List.of(trip1, trip2));

        BusInMaintenanceException ex = assertThrows(
                BusInMaintenanceException.class,
                () -> maintenanceService.scheduleMaintenance(request)
        );

        assertEquals(50L, ex.getBusId());
        assertEquals(List.of(201L, 202L), ex.getConflictingTripIds());
        assertTrue(ex.getMessage().contains("khoảng thời gian đệm 48h"));

        verify(maintenanceLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("scheduleMaintenance: Thành công khi không có chuyến đi nào trong khoảng đệm 48h")
    void testScheduleMaintenance_Success() {
        LocalDateTime start = LocalDateTime.now().plusDays(10);
        LocalDateTime end = start.plusDays(2);

        ScheduleMaintenanceRequest request = ScheduleMaintenanceRequest.builder()
                .busId(50L)
                .startTime(start)
                .endTime(end)
                .maintenanceType("PERIODIC")
                .cost(new BigDecimal("3000000.00"))
                .description("Thay nhớt và bảo dưỡng gầm")
                .build();

        when(busRepository.findById(50L)).thenReturn(Optional.of(bus));
        when(tripRepository.findConflictingTrips(eq(50L), eq(TripStatus.SCHEDULED), any(), any()))
                .thenReturn(List.of()); // Không có chuyến xung đột
        when(maintenanceLogRepository.hasOverlappingMaintenance(eq(50L), any(), any()))
                .thenReturn(false);

        MaintenanceLogEntity savedLog = MaintenanceLogEntity.builder()
                .id(1L)
                .bus(bus)
                .startTime(start)
                .endTime(end)
                .maintenanceType("PERIODIC")
                .cost(new BigDecimal("3000000.00"))
                .status(MaintenanceStatus.IN_PROGRESS)
                .build();

        when(maintenanceLogRepository.save(any(MaintenanceLogEntity.class))).thenReturn(savedLog);

        ScheduleMaintenanceResponse response = maintenanceService.scheduleMaintenance(request);

        assertNotNull(response);
        assertEquals(1L, response.getMaintenanceLogId());
        assertEquals(50L, response.getBusId());
        assertEquals("51B-888.88", response.getPlateNumber());
        assertEquals(MaintenanceStatus.IN_PROGRESS, response.getStatus());
    }
}
