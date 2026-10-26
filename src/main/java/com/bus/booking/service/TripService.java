package com.bus.booking.service;

import com.bus.booking.dto.request.TripSearchRequest;
import com.bus.booking.dto.response.TripSearchResponse;

import java.util.List;

public interface TripService {

    /**
     * Tìm kiếm danh sách chuyến xe theo điểm xuất phát, điểm đến và thời gian.
     * Tự động loại bỏ các chuyến có phương tiện xe đang dính lịch bảo dưỡng.
     */
    List<TripSearchResponse> searchTrips(TripSearchRequest request);

    /**
     * Lấy sơ đồ ghế và tình trạng từng ghế (AVAILABLE, HELD, BOOKED) của chuyến xe.
     */
    List<com.bus.booking.dto.response.TripSeatResponse> getTripSeats(Long tripId);
}
