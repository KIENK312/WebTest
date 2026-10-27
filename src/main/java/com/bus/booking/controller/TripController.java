package com.bus.booking.controller;

import com.bus.booking.dto.request.TripSearchRequest;
import com.bus.booking.dto.response.ApiResponse;
import com.bus.booking.dto.response.TripSearchResponse;
import com.bus.booking.dto.response.TripSeatResponse;
import com.bus.booking.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

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
     * Lấy sơ đồ ghế và tình trạng từng ghế (AVAILABLE, HELD, BOOKED) của chuyến xe.
     */
    @GetMapping("/{id}/seats")
    public ResponseEntity<ApiResponse<List<TripSeatResponse>>> getTripSeats(
            @PathVariable("id") Long tripId) {
        List<TripSeatResponse> seats = tripService.getTripSeats(tripId);
        return ResponseEntity.ok(ApiResponse.success("Lấy sơ đồ ghế thành công", seats));
    }
}
