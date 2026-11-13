package com.bus.booking.service.strategy;

import com.bus.booking.dto.request.HoldSeatRequest;
import com.bus.booking.entity.SeatEntity;
import com.bus.booking.entity.TripEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Strategy xử lý nghiệp vụ Đặt vé Xe Khách Giường Nằm Tiêu Chuẩn (38 Chỗ, 2 Tầng)
 */
@Slf4j
@Component("standardBusBookingStrategy")
public class StandardBusBookingStrategy implements TransportBookingStrategy {

    public static final String TYPE = "STANDARD_BUS";

    @Override
    public String getTransportType() {
        return TYPE;
    }

    @Override
    public String getDisplayName() {
        return "Xe Khách Giường Nằm Tiêu Chuẩn (38 chỗ)";
    }

    @Override
    public void validateBooking(TripEntity trip, SeatEntity seat, HoldSeatRequest request) {
        log.info("[Strategy - StandardBus] Xác thực đặt ghế: {} cho chuyến ID: {}", seat.getSeatNumber(), trip.getId());

        String seatNum = seat.getSeatNumber() != null ? seat.getSeatNumber().trim().toUpperCase() : "";
        if (!seatNum.matches("^[AB]\\d{2}$")) {
            log.warn("Mã ghế {} không đúng định dạng chuẩn xe khách giường nằm (A01-A21 hoặc B01-B21)", seatNum);
        }

        // Kiểm tra số tầng hợp lệ (Tầng 1: A, Tầng 2: B)
        if (seatNum.startsWith("A") && seat.getFloorNumber() != null && seat.getFloorNumber() != 1) {
            throw new IllegalArgumentException("Ghế dãy A phải thuộc Tầng 1 của xe khách!");
        }
        if (seatNum.startsWith("B") && seat.getFloorNumber() != null && seat.getFloorNumber() != 2) {
            throw new IllegalArgumentException("Ghế dãy B phải thuộc Tầng 2 của xe khách!");
        }
    }

    @Override
    public BigDecimal calculateFare(TripEntity trip, SeatEntity seat, HoldSeatRequest request) {
        BigDecimal base = trip.getBasePrice() != null ? trip.getBasePrice() : BigDecimal.ZERO;
        // Giá vé tiêu chuẩn không phụ thu
        return base;
    }

    @Override
    public String getCancellationPolicy() {
        return "Xe khách giường nằm tiêu chuẩn: Hủy vé trước giờ khởi hành > 24 giờ hoàn 100% cước phí; từ 2 - 24 giờ hoàn 70%; dưới 2 giờ không áp dụng hoàn tiền.";
    }

    @Override
    public List<String> getBusinessRules() {
        return List.of(
                "Sơ đồ 38 chỗ: Tầng dưới gồm 19 giường (A01-A21), tầng trên gồm 19 giường (B01-B21)",
                "Thời gian giữ chỗ tạm thời (Hold Seat): 15 phút chống nghẽn và xung đột ghế",
                "Hành lý miễn cước tối đa 20kg/hành khách",
                "Chính sách hoàn tiền: Hoàn 100% trước 24h, hoàn 70% trước 2h khởi hành"
        );
    }
}
