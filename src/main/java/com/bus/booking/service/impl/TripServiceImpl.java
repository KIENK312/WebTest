package com.bus.booking.service.impl;

import com.bus.booking.dto.request.TripSearchRequest;
import com.bus.booking.dto.response.TripSearchResponse;
import com.bus.booking.dto.response.TripSeatResponse;
import com.bus.booking.entity.BusEntity;
import com.bus.booking.entity.SeatEntity;
import com.bus.booking.entity.TicketEntity;
import com.bus.booking.entity.TripEntity;
import com.bus.booking.enums.TicketStatus;
import com.bus.booking.exception.ResourceNotFoundException;
import com.bus.booking.repository.MaintenanceLogRepository;
import com.bus.booking.repository.SeatRepository;
import com.bus.booking.repository.TicketRepository;
import com.bus.booking.repository.TripRepository;
import com.bus.booking.service.TripService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final MaintenanceLogRepository maintenanceLogRepository;
    private final TicketRepository ticketRepository;
    private final SeatRepository seatRepository;

    @Override
    public List<TripSearchResponse> searchTrips(TripSearchRequest request) {
        log.info("Tìm kiếm chuyến xe: origin={}, destination={}, date={}, start={}, end={}",
                request.getDeparturePoint(), request.getArrivalPoint(),
                request.getDepartureDate(), request.getStartTime(), request.getEndTime());

        // 1. Xác định khoảng thời gian tìm kiếm
        LocalDateTime startTime;
        LocalDateTime endTime;

        if (request.getDepartureDate() != null) {
            startTime = request.getDepartureDate().atStartOfDay();
            endTime = request.getDepartureDate().atTime(23, 59, 59);
        } else if (request.getStartTime() != null && request.getEndTime() != null) {
            startTime = request.getStartTime();
            endTime = request.getEndTime();
        } else {
            startTime = LocalDateTime.now();
            endTime = startTime.plusDays(30); // Tải toàn bộ chuyến xe mở bán trong 30 ngày
        }

        String origin = (request.getDeparturePoint() != null && !request.getDeparturePoint().isBlank())
                ? request.getDeparturePoint().trim() : null;
        String dest = (request.getArrivalPoint() != null && !request.getArrivalPoint().isBlank())
                ? request.getArrivalPoint().trim() : null;

        boolean isGeneralSearch = (origin == null && dest == null 
                && request.getDepartureDate() == null && request.getStartTime() == null);

        // 2. Truy vấn danh sách chuyến xe khớp tuyến đường và xe ACTIVE (JOIN FETCH đầy đủ, không N+1)
        List<TripEntity> rawTrips;
        if (isGeneralSearch) {
            // Trường hợp tải trang chung (trips.html không truyền lọc): Lấy TOÀN BỘ chuyến xe mở bán khả dụng
            rawTrips = tripRepository.findAllAvailableTrips();
        } else {
            rawTrips = tripRepository.findAvailableTrips(
                    origin,
                    dest,
                    startTime,
                    endTime
            );

            // Fallback linh hoạt: Nếu ngày tìm kiếm chính xác không còn chuyến, tìm các chuyến khả dụng không giới hạn ngày cho tuyến đó
            if (rawTrips.isEmpty() && (origin != null || dest != null)) {
                log.info("Không có chuyến trong ngày {}, tự động tìm kiếm tất cả chuyến xe khả dụng cho tuyến {} -> {}",
                        request.getDepartureDate(), origin, dest);
                rawTrips = tripRepository.findAvailableTrips(
                        origin,
                        dest,
                        null,
                        null
                );
            }
        }

        if (rawTrips.isEmpty()) {
            return List.of();
        }

        // 3. Tối ưu hóa: Lọc bỏ các chuyến xe có phương tiện đang trùng lịch bảo trì thực tế
        List<TripEntity> validTrips = rawTrips.stream()
                .filter(trip -> {
                    LocalDateTime tripStart = trip.getDepartureTime() != null ? trip.getDepartureTime() : startTime;
                    LocalDateTime tripEnd = trip.getArrivalTime() != null ? trip.getArrivalTime() : endTime;
                    return !maintenanceLogRepository.hasOverlappingMaintenance(trip.getBus().getId(), tripStart, tripEnd);
                })
                .toList();

        if (validTrips.isEmpty()) {
            return List.of();
        }

        // 3.1 Lọc theo mã hãng xe (FUTA, THANHBUOI) nếu có yêu cầu
        if (request.getOperatorCode() != null && !request.getOperatorCode().isBlank()) {
            String opCode = request.getOperatorCode().trim();
            validTrips = validTrips.stream()
                    .filter(t -> opCode.equalsIgnoreCase(t.getRoute().getOperator().getCode()))
                    .toList();
            if (validTrips.isEmpty()) {
                return List.of();
            }
        }

        // 4. Tối ưu hóa N+1: Gom đếm số ghế đã đặt cho toàn bộ chuyến bằng 1 câu SQL GROUP BY
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(10);
        List<Long> tripIds = validTrips.stream().map(TripEntity::getId).toList();
        List<Object[]> bookedSeatsCounts = ticketRepository.countBookedSeatsByTripIds(tripIds, cutoff);
        Map<Long, Long> bookedSeatsMap = (bookedSeatsCounts != null && !bookedSeatsCounts.isEmpty())
                ? bookedSeatsCounts.stream().collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]))
                : new java.util.HashMap<>();

        return validTrips.stream()
                .map(trip -> {
                    long bookedSeats = bookedSeatsMap.containsKey(trip.getId())
                            ? bookedSeatsMap.get(trip.getId())
                            : ticketRepository.countByTripIdAndStatusIn(trip.getId(), List.of(TicketStatus.CONFIRMED, TicketStatus.PENDING));
                    return mapToTripSearchResponse(trip, bookedSeats);
                })
                .toList();
    }

    @Override
    public List<TripSeatResponse> getTripSeats(Long tripId) {
        log.info("Lấy sơ đồ và trạng thái ghế của chuyến đi ID: {}", tripId);

        TripEntity trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến đi với ID: " + tripId));

        List<SeatEntity> seats = seatRepository.findByBusIdOrderBySeatNumberAsc(trip.getBus().getId());
        
        // Chỉ lấy các vé CONFIRMED hoặc PENDING còn trong thời hạn 10 phút (vé quá hạn tự động giải phóng)
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(10);
        List<TicketEntity> activeTickets = ticketRepository.findValidActiveTicketsByTripId(tripId, cutoff);

        Map<Long, TicketStatus> ticketStatusBySeatId = activeTickets.stream()
                .collect(Collectors.toMap(t -> t.getSeat().getId(), TicketEntity::getStatus, (s1, s2) -> s1));

        return seats.stream().map(seat -> {
            String status = "AVAILABLE";
            TicketStatus ticketStatus = ticketStatusBySeatId.get(seat.getId());
            if (ticketStatus == TicketStatus.CONFIRMED) {
                status = "BOOKED";
            } else if (ticketStatus == TicketStatus.PENDING) {
                status = "HELD";
            }

            return TripSeatResponse.builder()
                    .seatId(seat.getId())
                    .seatNumber(seat.getSeatNumber())
                    .seatType(seat.getSeatType())
                    .floorNumber(seat.getFloorNumber())
                    .price(trip.getBasePrice())
                    .status(status)
                    .build();
        }).toList();
    }

    private TripSearchResponse mapToTripSearchResponse(TripEntity trip, long bookedSeats) {
        BusEntity bus = trip.getBus();
        int totalSeats = bus.getTotalSeats();
        int availableSeats = Math.max(0, (int) (totalSeats - bookedSeats));

        return TripSearchResponse.builder()
                .tripId(trip.getId())
                .routeId(trip.getRoute().getId())
                .origin(trip.getRoute().getOrigin())
                .destination(trip.getRoute().getDestination())
                .operatorId(trip.getRoute().getOperator().getId())
                .operatorName(trip.getRoute().getOperator().getName())
                .operatorCode(trip.getRoute().getOperator().getCode())
                .busId(bus.getId())
                .busPlateNumber(bus.getPlateNumber())
                .busType(bus.getBusType())
                .departureTime(trip.getDepartureTime())
                .arrivalTime(trip.getArrivalTime())
                .basePrice(trip.getBasePrice())
                .status(trip.getStatus())
                .totalSeats(totalSeats)
                .availableSeats(availableSeats)
                .build();
    }
}
