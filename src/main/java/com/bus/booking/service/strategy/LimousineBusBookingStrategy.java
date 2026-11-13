package com.bus.booking.service.strategy;

import com.bus.booking.dto.request.HoldSeatRequest;
import com.bus.booking.entity.SeatEntity;
import com.bus.booking.entity.TripEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Strategy xử lý nghiệp vụ Đặt vé Xe Khách VIP Limousine / Cung Điện Di Động
 * (Chuyên biệt cho dòng xe khách cao cấp phòng nằm riêng biệt)
 */
@Slf4j
@Component("limousineBusBookingStrategy")
public class LimousineBusBookingStrategy implements TransportBookingStrategy {

    public static final String TYPE = "LIMOUSINE_BUS";

    @Override
    public String getTransportType() {
        return TYPE;
    }

    @Override
    public String getDisplayName() {
        return "Xe Khách VIP Limousine (Cabin Riêng Biệt)";
    }

    @Override
    public void validateBooking(TripEntity trip, SeatEntity seat, HoldSeatRequest request) {
        log.info("[Strategy - LimousineBus] Xác thực phòng VIP Limousine: {} cho chuyến ID: {}",
                seat.getSeatNumber(), trip.getId());

        if (request.getCustomerPhone() == null || request.getCustomerPhone().isBlank()) {
            throw new IllegalArgumentException("Hành khách đặt xe VIP Limousine bắt buộc phải cung cấp số điện thoại liên lạc tài xế đón tận nơi.");
        }
    }

    @Override
    public BigDecimal calculateFare(TripEntity trip, SeatEntity seat, HoldSeatRequest request) {
        BigDecimal base = trip.getBasePrice() != null ? trip.getBasePrice() : BigDecimal.ZERO;
        // Chuyến Limousine nếu cấu hình riêng có thể tính thêm phụ phí dịch vụ hoặc giữ nguyên base price
        return base;
    }

    @Override
    public String getCancellationPolicy() {
        return "Xe khách VIP Limousine: Hủy vé trước > 12 giờ hoàn 100%; từ 2 - 12 giờ hoàn 85%; dưới 2 giờ không áp dụng hoàn tiền.";
    }

    @Override
    public List<String> getBusinessRules() {
        return List.of(
                "Dịch vụ đón trả tận nơi trong nội thành và điểm du lịch",
                "Miễn phí nước suối, khăn lạnh, cổng sạc Type-C, tai nghe cao cấp và màn hình giải trí riêng",
                "Thời gian giữ chỗ tạm thời (Hold Seat): 15 phút bảo vệ giao dịch",
                "Hành lý miễn cước tối đa 30kg/hành khách"
        );
    }
}
