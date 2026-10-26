package com.bus.booking.service.impl;

import com.bus.booking.dto.response.*;
import com.bus.booking.entity.BusOperatorEntity;
import com.bus.booking.entity.TicketEntity;
import com.bus.booking.enums.OperatorStatus;
import com.bus.booking.enums.TicketStatus;
import com.bus.booking.exception.ResourceNotFoundException;
import com.bus.booking.repository.*;
import com.bus.booking.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final TicketRepository ticketRepository;
    private final BusOperatorRepository busOperatorRepository;
    private final BusRepository busRepository;
    private final RouteRepository routeRepository;
    private final TripRepository tripRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardResponseDto getDashboardOverview() {
        log.info("Đang tổng hợp dữ liệu thống kê Admin Dashboard thực tế từ SQL Server...");

        // 1. Thống kê vé và doanh thu thực tế (Dùng JPQL COALESCE)
        BigDecimal totalRevenue = ticketRepository.calculateTotalRevenue();
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        long totalTicketsSold = ticketRepository.countConfirmedTickets();
        long totalTickets = ticketRepository.count();
        long pendingTickets = ticketRepository.countByStatus(TicketStatus.PENDING);
        long cancelledTickets = ticketRepository.countByStatus(TicketStatus.CANCELLED);

        // 2. Thống kê tài nguyên hệ thống
        long totalTrips = tripRepository.count();
        long totalOperators = busOperatorRepository.count();
        long totalBuses = busRepository.count();
        long totalRoutes = routeRepository.count();

        // 3. Biểu đồ doanh thu 12 tháng thực tế của năm hiện tại
        int currentYear = LocalDate.now().getYear();
        LocalDateTime startOfYear = LocalDateTime.of(currentYear, 1, 1, 0, 0, 0);
        List<TicketEntity> yearTickets = ticketRepository.findConfirmedTicketsSince(startOfYear);

        Map<Integer, BigDecimal> monthlyRevenueMap = new HashMap<>();
        Map<Integer, Long> monthlyCountMap = new HashMap<>();
        for (int m = 1; m <= 12; m++) {
            monthlyRevenueMap.put(m, BigDecimal.ZERO);
            monthlyCountMap.put(m, 0L);
        }

        for (TicketEntity t : yearTickets) {
            if (t.getBookingTime() != null) {
                int month = t.getBookingTime().getMonthValue();
                BigDecimal currentRev = monthlyRevenueMap.get(month);
                BigDecimal ticketPrice = t.getPrice() != null ? t.getPrice() : BigDecimal.ZERO;
                monthlyRevenueMap.put(month, currentRev.add(ticketPrice));
                monthlyCountMap.put(month, monthlyCountMap.get(month) + 1);
            }
        }

        List<MonthlyRevenueDto> chartData = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            chartData.add(MonthlyRevenueDto.builder()
                    .month("T" + m)
                    .monthNumber(m)
                    .year(currentYear)
                    .revenue(monthlyRevenueMap.get(m))
                    .ticketCount(monthlyCountMap.get(m))
                    .build());
        }

        // 4. Danh sách vé gần đây nhất (tối đa 10 vé)
        List<TicketEntity> recentTicketEntities = ticketRepository.findRecentTicketsWithDetails(PageRequest.of(0, 10));
        List<HoldSeatResponse> recentTickets = recentTicketEntities.stream()
                .map(this::toHoldSeatResponse)
                .collect(Collectors.toList());

        // 5. Danh sách đối tác nhà xe
        List<BusOperatorEntity> operatorEntities = busOperatorRepository.findAll();
        List<PartnerDto> operatorsList = operatorEntities.stream()
                .map(this::toPartnerDto)
                .collect(Collectors.toList());

        return AdminDashboardResponseDto.builder()
                .totalRevenue(totalRevenue)
                .totalTicketsSold(totalTicketsSold)
                .totalTickets(totalTickets)
                .pendingTickets(pendingTickets)
                .cancelledTickets(cancelledTickets)
                .totalTrips(totalTrips)
                .totalOperators(totalOperators)
                .totalBuses(totalBuses)
                .totalRoutes(totalRoutes)
                .chartData(chartData)
                .recentTickets(recentTickets)
                .operatorsList(operatorsList)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartnerDto> getAllPartners() {
        return busOperatorRepository.findAll().stream()
                .map(this::toPartnerDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PartnerDto updatePartnerStatus(Long partnerId, String status) {
        BusOperatorEntity partner = busOperatorRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đối tác với ID: " + partnerId));

        try {
            partner.setStatus(OperatorStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Trạng thái đối tác không hợp lệ: " + status + ". Chấp nhận: ACTIVE, SUSPENDED, INACTIVE");
        }

        BusOperatorEntity updated = busOperatorRepository.save(partner);
        log.info("Đã cập nhật trạng thái đối tác '{}' sang: {}", updated.getName(), updated.getStatus());
        return toPartnerDto(updated);
    }

    @Override
    @Transactional
    public PartnerDto createPartner(PartnerDto request) {
        if (busOperatorRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Mã nhà xe '" + request.getCode() + "' đã tồn tại!");
        }

        BusOperatorEntity entity = BusOperatorEntity.builder()
                .name(request.getName())
                .code(request.getCode().toUpperCase().trim())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .address(request.getAddress())
                .status(request.getStatus() != null ? request.getStatus() : OperatorStatus.ACTIVE)
                .build();

        BusOperatorEntity saved = busOperatorRepository.save(entity);
        log.info("Đã tạo mới đối tác nhà xe ID #{}: '{}'", saved.getId(), saved.getName());
        return toPartnerDto(saved);
    }

    // ============================================================
    // MAPPER FUNCTIONS (Null-safe)
    // ============================================================

    private PartnerDto toPartnerDto(BusOperatorEntity p) {
        if (p == null) return null;
        return PartnerDto.builder()
                .id(p.getId())
                .name(p.getName())
                .code(p.getCode())
                .contactEmail(p.getContactEmail())
                .contactPhone(p.getContactPhone())
                .address(p.getAddress())
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private HoldSeatResponse toHoldSeatResponse(TicketEntity t) {
        if (t == null) return null;
        return HoldSeatResponse.builder()
                .ticketId(t.getId())
                .ticketCode(t.getTicketCode())
                .tripId(t.getTrip() != null ? t.getTrip().getId() : null)
                .seatId(t.getSeat() != null ? t.getSeat().getId() : null)
                .seatNumber(t.getSeat() != null ? t.getSeat().getSeatNumber() : "—")
                .customerName(t.getCustomerName())
                .customerPhone(t.getCustomerPhone())
                .customerEmail(t.getCustomerEmail())
                .price(t.getPrice())
                .status(t.getStatus())
                .version(t.getVersion())
                .bookingTime(t.getBookingTime())
                .holdExpiresAt(t.getBookingTime() != null ? t.getBookingTime().plusMinutes(15) : null)
                .message("Thông tin vé tra cứu")
                .build();
    }
}
