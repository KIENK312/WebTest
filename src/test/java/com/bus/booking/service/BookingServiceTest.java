package com.bus.booking.service;

import com.bus.booking.dto.request.HoldSeatRequest;
import com.bus.booking.dto.response.HoldSeatResponse;
import com.bus.booking.entity.BusEntity;
import com.bus.booking.entity.SeatEntity;
import com.bus.booking.entity.TicketEntity;
import com.bus.booking.entity.TripEntity;
import com.bus.booking.enums.TicketStatus;
import com.bus.booking.enums.TripStatus;
import com.bus.booking.exception.ResourceNotFoundException;
import com.bus.booking.exception.SeatAlreadyBookedException;
import com.bus.booking.repository.SeatRepository;
import com.bus.booking.repository.TicketRepository;
import com.bus.booking.repository.TripRepository;
import com.bus.booking.repository.UserRepository;
import com.bus.booking.service.impl.BookingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private TripEntity trip;
    private SeatEntity seat;
    private BusEntity bus;

    @BeforeEach
    void setUp() {
        bus = BusEntity.builder().id(10L).plateNumber("51B-999.99").build();

        trip = TripEntity.builder()
                .id(1L)
                .bus(bus)
                .basePrice(new BigDecimal("350000.00"))
                .status(TripStatus.SCHEDULED)
                .build();

        seat = SeatEntity.builder()
                .id(20L)
                .bus(bus)
                .seatNumber("A01")
                .floorNumber(1)
                .build();
    }

    @Test
    @DisplayName("holdSeat: Giữ chỗ thành công khi ghế còn trống")
    void testHoldSeat_Success() {
        HoldSeatRequest request = HoldSeatRequest.builder()
                .tripId(1L)
                .seatId(20L)
                .customerName("Nguyễn Văn A")
                .customerPhone("0901234567")
                .build();

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(seatRepository.findById(20L)).thenReturn(Optional.of(seat));
        when(ticketRepository.existsByTripIdAndSeatIdAndStatusIn(eq(1L), eq(20L), anyList()))
                .thenReturn(false);

        TicketEntity savedTicket = TicketEntity.builder()
                .id(100L)
                .trip(trip)
                .seat(seat)
                .customerName("Nguyễn Văn A")
                .customerPhone("0901234567")
                .ticketCode("TK-T1-A01-ABCDEF")
                .price(new BigDecimal("350000.00"))
                .status(TicketStatus.PENDING)
                .version(0L)
                .bookingTime(LocalDateTime.now())
                .build();

        when(ticketRepository.saveAndFlush(any(TicketEntity.class))).thenReturn(savedTicket);

        HoldSeatResponse response = bookingService.holdSeat(request);

        assertNotNull(response);
        assertEquals(100L, response.getTicketId());
        assertEquals("A01", response.getSeatNumber());
        assertEquals(TicketStatus.PENDING, response.getStatus());
        assertEquals(0L, response.getVersion());
        assertNotNull(response.getHoldExpiresAt());
    }

    @Test
    @DisplayName("holdSeat: Thất bại và ném SeatAlreadyBookedException khi ghế đã có người giữ/đặt")
    void testHoldSeat_ThrowsWhenSeatAlreadyHeldOrBooked() {
        HoldSeatRequest request = HoldSeatRequest.builder()
                .tripId(1L)
                .seatId(20L)
                .customerName("Trần Văn B")
                .customerPhone("0912345678")
                .build();

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(seatRepository.findById(20L)).thenReturn(Optional.of(seat));
        when(ticketRepository.existsByTripIdAndSeatIdAndStatusIn(eq(1L), eq(20L), anyList()))
                .thenReturn(true); // Ghế đã bị giữ

        assertThrows(SeatAlreadyBookedException.class, () -> bookingService.holdSeat(request));
        verify(ticketRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("holdSeat: Bắt OptimisticLockingFailureException khi 2 người cùng đặt 1 lúc")
    void testHoldSeat_HandlesOptimisticLockingFailure() {
        HoldSeatRequest request = HoldSeatRequest.builder()
                .tripId(1L)
                .seatId(20L)
                .customerName("Lê Văn C")
                .customerPhone("0923456789")
                .build();

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(seatRepository.findById(20L)).thenReturn(Optional.of(seat));
        when(ticketRepository.existsByTripIdAndSeatIdAndStatusIn(eq(1L), eq(20L), anyList()))
                .thenReturn(false);

        // Giả lập xung đột đồng thời trong Database (Optimistic Locking)
        when(ticketRepository.saveAndFlush(any(TicketEntity.class)))
                .thenThrow(new OptimisticLockingFailureException("Row was updated or deleted by another transaction"));

        SeatAlreadyBookedException exception = assertThrows(
                SeatAlreadyBookedException.class,
                () -> bookingService.holdSeat(request)
        );

        assertTrue(exception.getMessage().contains("người khác giữ chỗ trong cùng một thời điểm"));
    }
}
