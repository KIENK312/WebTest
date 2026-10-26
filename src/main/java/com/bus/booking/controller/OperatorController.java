package com.bus.booking.controller;

import com.bus.booking.dto.request.CreateTripRequest;
import com.bus.booking.dto.response.ApiResponse;
import com.bus.booking.dto.response.BusDto;
import com.bus.booking.dto.response.MaintenanceDto;
import com.bus.booking.dto.response.RouteDto;
import com.bus.booking.dto.response.TripSearchResponse;
import com.bus.booking.service.OperatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/operator")
@RequiredArgsConstructor
public class OperatorController {

    private final OperatorService operatorService;

    /**
     * Lấy danh sách đội xe khách phẳng (BusDto) theo nhà xe hoặc toàn hệ thống.
     * Sử dụng JOIN FETCH, hoàn toàn không gây LazyInitializationException.
     */
    @GetMapping("/buses")
    public ResponseEntity<ApiResponse<List<BusDto>>> getBuses(
            @RequestParam(value = "operatorId", required = false) Long operatorId) {
        List<BusDto> result = operatorService.getBuses(operatorId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đội xe thành công", result));
    }

    /**
     * Bật / tắt trạng thái bảo dưỡng của xe (ACTIVE <-> MAINTENANCE).
     */
    @PutMapping("/buses/{id}/status")
    public ResponseEntity<ApiResponse<BusDto>> updateBusStatus(
            @PathVariable("id") Long busId,
            @RequestParam(value = "status", required = false) String statusParam,
            @RequestBody(required = false) java.util.Map<String, String> body) {
        String statusStr = (statusParam != null && !statusParam.isBlank())
                ? statusParam
                : (body != null ? body.get("status") : null);
        if (statusStr == null || statusStr.isBlank()) {
            throw new IllegalArgumentException("Tham số status không được để trống");
        }
        BusDto res = operatorService.updateBusStatus(busId, statusStr);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái xe thành công", res));
    }

    /**
     * Lấy danh sách tuyến đường phẳng (RouteDto).
     * Trả về DTO phẳng, tuyệt đối không trả JPA Entity.
     */
    @GetMapping("/routes")
    public ResponseEntity<ApiResponse<List<RouteDto>>> getRoutes(
            @RequestParam(value = "operatorId", required = false) Long operatorId) {
        List<RouteDto> routes = operatorService.getRoutes(operatorId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tuyến đường thành công", routes));
    }

    /**
     * Lấy danh sách chuyến xe của nhà xe (TripSearchResponse).
     */
    @GetMapping("/trips")
    public ResponseEntity<ApiResponse<List<TripSearchResponse>>> getTrips(
            @RequestParam(value = "operatorId", required = false) Long operatorId) {
        List<TripSearchResponse> responses = operatorService.getTrips(operatorId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách chuyến xe thành công", responses));
    }

    /**
     * Tạo chuyến xe mới kèm kiểm tra xung đột bảo trì.
     */
    @PostMapping("/trips")
    public ResponseEntity<ApiResponse<TripSearchResponse>> createTrip(
            @Valid @RequestBody CreateTripRequest request) {
        TripSearchResponse response = operatorService.createTrip(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Tạo chuyến xe mới thành công", response));
    }


}
