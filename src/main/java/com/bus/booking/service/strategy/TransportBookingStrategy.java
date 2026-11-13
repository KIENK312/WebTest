package com.bus.booking.service.strategy;

import com.bus.booking.dto.request.HoldSeatRequest;
import com.bus.booking.entity.SeatEntity;
import com.bus.booking.entity.TripEntity;

import java.math.BigDecimal;
import java.util.List;

/**
 * Strategy Pattern Interface cho nghiệp vụ đặt vé phương tiện Xe Khách
 * (Đáp ứng Khối IV - Kiến trúc hướng đối tượng & Design Patterns trong đề cương).
 */
public interface TransportBookingStrategy {

    /**
     * Tên định danh của chủng loại xe khách (ví dụ: STANDARD_BUS, LIMOUSINE_BUS)
     */
    String getTransportType();

    /**
     * Tên hiển thị thân thiện tiếng Việt
     */
    String getDisplayName();

    /**
     * Kiểm tra tính hợp lệ của ghế ngồi và quy định nghiệp vụ đối với loại xe khách này
     */
    void validateBooking(TripEntity trip, SeatEntity seat, HoldSeatRequest request);

    /**
     * Tính toán giá vé thực tế dựa trên giá vé cơ sở của chuyến và chính sách của loại xe
     */
    BigDecimal calculateFare(TripEntity trip, SeatEntity seat, HoldSeatRequest request);

    /**
     * Chính sách hủy đổi trả vé áp dụng riêng cho loại xe khách này
     */
    String getCancellationPolicy();

    /**
     * Danh sách các quy tắc nghiệp vụ đặc thù
     */
    List<String> getBusinessRules();
}
