package com.bus.booking.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardResponseDto {
    private BigDecimal totalRevenue;       // Tổng doanh thu thực tế từ vé CONFIRMED (COALESCE 0)
    private long totalTicketsSold;         // Tổng số vé đã bán thành công (CONFIRMED)
    private long totalTickets;             // Tổng số vé trong hệ thống
    private long pendingTickets;           // Số vé đang chờ thanh toán
    private long cancelledTickets;         // Số vé đã hủy
    private long totalTrips;               // Tổng số chuyến xe hiện có
    private long totalOperators;           // Tổng số nhà xe đối tác
    private long totalBuses;               // Tổng số xe khách
    private long totalRoutes;              // Tổng số tuyến đường

    private List<MonthlyRevenueDto> chartData;     // Biểu đồ doanh thu 12 tháng thực tế
    private List<HoldSeatResponse> recentTickets;  // Danh sách vé gần đây
    private List<PartnerDto> operatorsList;        // Danh sách đối tác nhà xe
}
