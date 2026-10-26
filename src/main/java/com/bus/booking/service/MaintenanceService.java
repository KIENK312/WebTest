package com.bus.booking.service;

import com.bus.booking.dto.request.ScheduleMaintenanceRequest;
import com.bus.booking.dto.response.ScheduleMaintenanceResponse;

public interface MaintenanceService {

    /**
     * Lên lịch bảo trì cho xe khách theo quy tắc an toàn đệm 48h:
     * Kiểm tra và từ chối nếu có chuyến đi SCHEDULED trong khoảng [Start - 48h, End + 48h].
     */
    ScheduleMaintenanceResponse scheduleMaintenance(ScheduleMaintenanceRequest request);

    /**
     * Lấy danh sách lịch sử bảo dưỡng của một xe.
     */
    java.util.List<ScheduleMaintenanceResponse> getMaintenanceLogsByBus(Long busId);
}
