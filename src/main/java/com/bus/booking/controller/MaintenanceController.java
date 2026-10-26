package com.bus.booking.controller;

import com.bus.booking.dto.request.ScheduleMaintenanceRequest;
import com.bus.booking.dto.response.ApiResponse;
import com.bus.booking.dto.response.ScheduleMaintenanceResponse;
import com.bus.booking.service.MaintenanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/operator/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    /**
     * Lên lịch bảo trì xe khách theo quy tắc an toàn đệm 48h.
     * Chỉ dành cho tài khoản có vai trò ROLE_OPERATOR hoặc ROLE_ADMIN.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ScheduleMaintenanceResponse>> scheduleMaintenance(
            @Valid @RequestBody ScheduleMaintenanceRequest request) {
        ScheduleMaintenanceResponse response = maintenanceService.scheduleMaintenance(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Lên lịch bảo trì xe thành công", response));
    }

    /**
     * Xem toàn bộ nhật ký lịch sử bảo dưỡng của một xe khách.
     */
    @GetMapping("/bus/{busId}")
    public ResponseEntity<ApiResponse<List<ScheduleMaintenanceResponse>>> getMaintenanceLogsByBus(
            @PathVariable("busId") Long busId) {
        List<ScheduleMaintenanceResponse> logs = maintenanceService.getMaintenanceLogsByBus(busId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách nhật ký bảo trì thành công", logs));
    }
}
