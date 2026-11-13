package com.bus.booking.controller;

import com.bus.booking.dto.request.TripSearchRequest;
import com.bus.booking.dto.response.ApiResponse;
import com.bus.booking.dto.response.TripSearchResponse;
import com.bus.booking.dto.response.TripSeatResponse;
import com.bus.booking.service.TripService;
import com.bus.booking.service.strategy.TransportBookingStrategy;
import com.bus.booking.service.strategy.TransportStrategyFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;
    private final TransportStrategyFactory transportStrategyFactory;

    /**
     * Tìm kiếm và lấy danh sách chuyến xe công khai (hỗ trợ lấy tất cả chuyến đang mở bán).
     * Tự động loại bỏ các chuyến có xe đang dính bảo dưỡng.
     */
    @GetMapping({"", "/all", "/search"})
    public ResponseEntity<ApiResponse<List<TripSearchResponse>>> searchTrips(
            @Valid @ModelAttribute TripSearchRequest request) {
        List<TripSearchResponse> responses = tripService.searchTrips(request);
        return ResponseEntity.ok(ApiResponse.success(
                String.format("Tìm thấy %d chuyến xe khả dụng", responses.size()),
                responses
        ));
    }

    /**
     * Lấy thông tin chi tiết một chuyến xe.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TripSearchResponse>> getTripById(
            @PathVariable("id") Long tripId) {
        TripSearchResponse response = tripService.getTripById(tripId);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin chuyến xe thành công", response));
    }

    /**
     * Lấy sơ đồ ghế và tình trạng từng ghế (AVAILABLE, HELD, BOOKED) của chuyến xe.
     */
    @GetMapping("/{id}/seats")
    public ResponseEntity<ApiResponse<List<TripSeatResponse>>> getTripSeats(
            @PathVariable("id") Long tripId) {
        List<TripSeatResponse> seats = tripService.getTripSeats(tripId);
        return ResponseEntity.ok(ApiResponse.success("Lấy sơ đồ ghế thành công", seats));
    }

    /**
     * Tra cứu chính sách và quy tắc đặt vé theo Strategy Pattern của chuyến xe khách (Khối IV).
     */
    @GetMapping("/{id}/policy")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTripBookingPolicy(
            @PathVariable("id") Long tripId) {
        TripSearchResponse trip = tripService.getTripById(tripId);
        TransportBookingStrategy strategy = transportStrategyFactory.getStrategy(trip.getBusType());

        Map<String, Object> policyData = Map.of(
                "tripId", tripId,
                "busType", trip.getBusType() != null ? trip.getBusType() : "STANDARD_BUS",
                "strategyType", strategy.getTransportType(),
                "strategyName", strategy.getDisplayName(),
                "cancellationPolicy", strategy.getCancellationPolicy(),
                "businessRules", strategy.getBusinessRules()
        );

        return ResponseEntity.ok(ApiResponse.success("Lấy chính sách đặt vé theo Strategy thành công", policyData));
    }

    /**
     * Liệt kê các chiến lược đặt vé xe khách đang áp dụng trong hệ thống (Strategy Pattern Showcase).
     */
    @GetMapping("/booking-strategies")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllBookingStrategies() {
        List<Map<String, Object>> list = transportStrategyFactory.getAllStrategies().stream()
                .map(s -> Map.of(
                        "type", (Object) s.getTransportType(),
                        "displayName", s.getDisplayName(),
                        "cancellationPolicy", s.getCancellationPolicy(),
                        "businessRules", s.getBusinessRules()
                ))
                .toList();

        return ResponseEntity.ok(ApiResponse.success("Danh sách các Strategy nghiệp vụ xe khách", list));
    }
}
