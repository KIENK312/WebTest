package com.bus.booking.service;

import com.bus.booking.dto.request.CreateTripRequest;
import com.bus.booking.dto.response.BusDto;
import com.bus.booking.dto.response.MaintenanceDto;
import com.bus.booking.dto.response.RouteDto;
import com.bus.booking.dto.response.TripSearchResponse;

import java.util.List;

public interface OperatorService {

    /**
     * Lấy danh sách xe của nhà xe (hoặc tất cả nếu là admin).
     * Tự động fallback lấy nhà xe hợp lệ đầu tiên nếu operatorId không hợp lệ.
     */
    List<BusDto> getBuses(Long operatorId);

    /**
     * Bật / tắt trạng thái bảo dưỡng của xe (ACTIVE <-> MAINTENANCE).
     */
    BusDto updateBusStatus(Long busId, String status);

    /**
     * Lấy danh sách tuyến đường của nhà xe (hoặc tất cả).
     */
    List<RouteDto> getRoutes(Long operatorId);

    /**
     * Lấy danh sách chuyến xe của nhà xe.
     */
    List<TripSearchResponse> getTrips(Long operatorId);

    /**
     * Tạo chuyến xe mới với kiểm tra đệm 48h và an toàn bảo trì.
     */
    TripSearchResponse createTrip(CreateTripRequest request);

    /**
     * Xem nhật ký bảo trì của xe.
     */
    List<MaintenanceDto> getMaintenanceLogsByBus(Long busId);

    /**
     * Lấy danh sách đối tác nhà xe phù hợp theo phân quyền của người đang đăng nhập:
     * - Admin: Toàn bộ danh sách nhà xe hợp tác
     * - Operator (Phương Trang, Hoàng Long...): Duy nhất nhà xe của họ
     */
    List<com.bus.booking.dto.response.PartnerDto> getPartnersForCurrentScope();

    /**
     * Xử lý giải quyết operatorId an toàn theo phân quyền người dùng.
     */
    Long resolveOperatorId(Long operatorId);
}
