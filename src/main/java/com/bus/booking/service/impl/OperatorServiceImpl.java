package com.bus.booking.service.impl;

import com.bus.booking.dto.request.CreateTripRequest;
import com.bus.booking.dto.response.BusDto;
import com.bus.booking.dto.response.MaintenanceDto;
import com.bus.booking.dto.response.RouteDto;
import com.bus.booking.dto.response.TripSearchResponse;
import com.bus.booking.entity.BusEntity;
import com.bus.booking.entity.BusOperatorEntity;
import com.bus.booking.entity.MaintenanceLogEntity;
import com.bus.booking.entity.RouteEntity;
import com.bus.booking.entity.TripEntity;
import com.bus.booking.enums.BusStatus;
import com.bus.booking.enums.TripStatus;
import com.bus.booking.exception.BusInMaintenanceException;
import com.bus.booking.exception.ResourceNotFoundException;
import com.bus.booking.repository.*;
import com.bus.booking.service.OperatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperatorServiceImpl implements OperatorService {

    private final BusRepository busRepository;
    private final RouteRepository routeRepository;
    private final TripRepository tripRepository;
    private final MaintenanceLogRepository maintenanceLogRepository;
    private final BusOperatorRepository busOperatorRepository;
    private final TicketRepository ticketRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BusDto> getBuses(Long operatorId) {
        Long resolvedId = resolveOperatorId(operatorId);
        List<BusEntity> buses = (resolvedId != null)
                ? busRepository.findByOperatorIdWithOperator(resolvedId)
                : busRepository.findAllWithOperator();

        return buses.stream()
                .map(this::toBusDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BusDto updateBusStatus(Long busId, String status) {
        BusEntity bus = busRepository.findByIdWithOperator(busId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy xe với ID: " + busId));

        try {
            BusStatus newStatus = BusStatus.valueOf(status.toUpperCase());
            bus.setStatus(newStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Trạng thái xe không hợp lệ: " + status + ". Chấp nhận: ACTIVE, MAINTENANCE, INACTIVE");
        }

        BusEntity saved = busRepository.save(bus);
        log.info("Đã cập nhật trạng thái xe '{}' sang: {}", saved.getPlateNumber(), saved.getStatus());
        return toBusDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RouteDto> getRoutes(Long operatorId) {
        Long resolvedId = resolveOperatorId(operatorId);
        List<RouteEntity> routes = (resolvedId != null)
                ? routeRepository.findByOperatorIdWithOperator(resolvedId)
                : routeRepository.findAllWithOperator();

        return routes.stream()
                .map(this::toRouteDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripSearchResponse> getTrips(Long operatorId) {
        Long resolvedId = resolveOperatorId(operatorId);
        List<TripEntity> trips = (resolvedId != null)
                ? tripRepository.findByOperatorIdWithDetails(resolvedId)
                : tripRepository.findAllWithDetails();

        if (trips.isEmpty()) {
            return List.of();
        }

        // Tối ưu N+1: Đếm số vé đã đặt cho toàn bộ chuyến bằng 1 câu SQL GROUP BY
        List<Long> tripIds = trips.stream().map(TripEntity::getId).toList();
        java.time.LocalDateTime cutoff = java.time.LocalDateTime.now().minusMinutes(10);
        java.util.Map<Long, Long> bookedSeatsMap = ticketRepository.countBookedSeatsByTripIds(tripIds, cutoff).stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));

        return trips.stream()
                .map(t -> toTripResponse(t, bookedSeatsMap.getOrDefault(t.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TripSearchResponse createTrip(CreateTripRequest request) {
        if (!request.getArrivalTime().isAfter(request.getDepartureTime())) {
            throw new IllegalArgumentException("Giờ đến dự kiến phải sau giờ xuất bến!");
        }

        BusEntity bus = busRepository.findByIdWithOperator(request.getBusId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy xe với ID: " + request.getBusId()));

        RouteEntity route = routeRepository.findByIdWithOperator(request.getRouteId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tuyến đường với ID: " + request.getRouteId()));

        // Kiểm tra trạng thái xe
        if (bus.getStatus() == BusStatus.MAINTENANCE) {
            throw new BusInMaintenanceException(
                    bus.getId(),
                    String.format("Phương tiện '%s' đang ở trạng thái bảo dưỡng (MAINTENANCE). Không thể xếp chuyến mới!",
                            bus.getPlateNumber())
            );
        }

        // Kiểm tra lịch bảo trì đệm
        boolean hasOverlap = maintenanceLogRepository.hasOverlappingMaintenance(
                bus.getId(), request.getDepartureTime(), request.getArrivalTime());
        if (hasOverlap) {
            throw new BusInMaintenanceException(
                    bus.getId(),
                    String.format("Phương tiện '%s' có lịch bảo trì trùng với thời gian chuyến xe dự kiến!",
                            bus.getPlateNumber())
            );
        }

        TripEntity trip = TripEntity.builder()
                .route(route)
                .bus(bus)
                .departureTime(request.getDepartureTime())
                .arrivalTime(request.getArrivalTime())
                .basePrice(request.getBasePrice())
                .status(TripStatus.SCHEDULED)
                .build();

        TripEntity saved = tripRepository.save(trip);
        log.info("Đã tạo chuyến xe mới ID #{} cho xe '{}' trên tuyến '{} -> {}'",
                saved.getId(), bus.getPlateNumber(), route.getOrigin(), route.getDestination());

        return toTripResponse(saved, 0L);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceDto> getMaintenanceLogsByBus(Long busId) {
        List<MaintenanceLogEntity> logs = maintenanceLogRepository.findByBusIdWithDetails(busId);
        return logs.stream()
                .map(this::toMaintenanceDto)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách đối tác nhà xe theo phân quyền người dùng:
     * - Admin: Toàn bộ danh sách nhà xe đang hợp tác
     * - Operator (Phương Trang, Hoàng Long...): Duy nhất nhà xe của họ
     */
    @Override
    @Transactional(readOnly = true)
    public List<com.bus.booking.dto.response.PartnerDto> getPartnersForCurrentScope() {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof com.bus.booking.security.CustomUserDetails userDetails) {
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin && userDetails.getOperatorId() != null) {
                // OPERATOR: Chỉ trả về đối tác của chính mình
                return busOperatorRepository.findById(userDetails.getOperatorId())
                        .map(op -> List.of(toPartnerDto(op)))
                        .orElse(List.of());
            }
        }

        // ADMIN: Trả về toàn bộ danh sách nhà xe đối tác
        return busOperatorRepository.findAll().stream()
                .map(this::toPartnerDto)
                .collect(Collectors.toList());
    }

    /**
     * Xử lý giải quyết operatorId an toàn theo phân quyền:
     * - Nếu đăng nhập tài khoản ADMIN: có thể xem toàn bộ (khi operatorId == null) hoặc xem bất kỳ nhà xe nào
     * - Nếu đăng nhập tài khoản OPERATOR: BẮT BUỘC chỉ được xem nhà xe của họ
     */
    @Override
    @Transactional(readOnly = true)
    public Long resolveOperatorId(Long operatorId) {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof com.bus.booking.security.CustomUserDetails userDetails) {
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (isAdmin) {
                if (operatorId != null && busOperatorRepository.existsById(operatorId)) {
                    return operatorId;
                }
                return null; // Admin không chọn nhà xe cụ thể -> xem toàn bộ hệ thống
            }

            // Đối tác nhà xe (OPERATOR): Luôn luôn gắn chặt với nhà xe của mình
            if (userDetails.getOperatorId() != null) {
                return userDetails.getOperatorId();
            }
        }

        if (operatorId != null && busOperatorRepository.existsById(operatorId)) {
            return operatorId;
        }
        return null;
    }

    private com.bus.booking.dto.response.PartnerDto toPartnerDto(BusOperatorEntity op) {
        if (op == null) return null;
        return com.bus.booking.dto.response.PartnerDto.builder()
                .id(op.getId())
                .name(op.getName())
                .code(op.getCode())
                .contactPhone(op.getContactPhone())
                .contactEmail(op.getContactEmail())
                .address(op.getAddress())
                .status(op.getStatus() != null ? op.getStatus() : com.bus.booking.enums.OperatorStatus.ACTIVE)
                .createdAt(op.getCreatedAt())
                .build();
    }

    // ============================================================
    // MAPPER FUNCTIONS (Hoàn toàn null-safe)
    // ============================================================

    private BusDto toBusDto(BusEntity b) {
        if (b == null) return null;
        BusOperatorEntity op = b.getOperator();
        return BusDto.builder()
                .id(b.getId())
                .operatorId(op != null ? op.getId() : null)
                .operatorName(op != null ? op.getName() : "—")
                .operatorCode(op != null ? op.getCode() : "—")
                .plateNumber(b.getPlateNumber())
                .busType(b.getBusType())
                .totalSeats(b.getTotalSeats())
                .status(b.getStatus())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }

    private RouteDto toRouteDto(RouteEntity r) {
        if (r == null) return null;
        BusOperatorEntity op = r.getOperator();
        return RouteDto.builder()
                .id(r.getId())
                .operatorId(op != null ? op.getId() : null)
                .operatorName(op != null ? op.getName() : "—")
                .operatorCode(op != null ? op.getCode() : "—")
                .origin(r.getOrigin())
                .destination(r.getDestination())
                .distanceKm(r.getDistanceKm())
                .estimatedDurationHours(r.getEstimatedDurationHours())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .build();
    }

    private TripSearchResponse toTripResponse(TripEntity t, long bookedSeats) {
        if (t == null) return null;
        RouteEntity route = t.getRoute();
        BusEntity bus = t.getBus();
        BusOperatorEntity op = (bus != null) ? bus.getOperator() : null;
        int totalSeats = bus != null && bus.getTotalSeats() != null ? bus.getTotalSeats() : 0;
        int availableSeats = Math.max(0, (int) (totalSeats - bookedSeats));

        return TripSearchResponse.builder()
                .tripId(t.getId())
                .routeId(route != null ? route.getId() : null)
                .origin(route != null ? route.getOrigin() : "—")
                .destination(route != null ? route.getDestination() : "—")
                .operatorId(op != null ? op.getId() : null)
                .operatorName(op != null ? op.getName() : "—")
                .operatorCode(op != null ? op.getCode() : "—")
                .busId(bus != null ? bus.getId() : null)
                .busPlateNumber(bus != null ? bus.getPlateNumber() : "—")
                .busType(bus != null ? bus.getBusType() : "—")
                .departureTime(t.getDepartureTime())
                .arrivalTime(t.getArrivalTime())
                .basePrice(t.getBasePrice())
                .status(t.getStatus())
                .totalSeats(totalSeats)
                .availableSeats(availableSeats)
                .build();
    }

    private MaintenanceDto toMaintenanceDto(MaintenanceLogEntity m) {
        if (m == null) return null;
        BusEntity bus = m.getBus();
        BusOperatorEntity op = (bus != null) ? bus.getOperator() : null;

        return MaintenanceDto.builder()
                .id(m.getId())
                .busId(bus != null ? bus.getId() : null)
                .busPlateNumber(bus != null ? bus.getPlateNumber() : "—")
                .busType(bus != null ? bus.getBusType() : "—")
                .operatorId(op != null ? op.getId() : null)
                .operatorName(op != null ? op.getName() : "—")
                .startTime(m.getStartTime())
                .endTime(m.getEndTime())
                .maintenanceType(m.getMaintenanceType())
                .cost(m.getCost())
                .description(m.getDescription())
                .status(m.getStatus())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
