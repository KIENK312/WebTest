package com.bus.booking.service.strategy;

import com.bus.booking.dto.request.HoldSeatRequest;
import com.bus.booking.entity.SeatEntity;
import com.bus.booking.entity.TripEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Strategy xử lý nghiệp vụ Đặt vé Tàu Hỏa Du Lịch (Nhóm phương tiện 2/4 theo chuẩn Khối IV của đề cương)
 * Đáp ứng nghiệp vụ liên kết vé tàu hỏa đường sắt du lịch (Toa nằm mềm điều hòa & Toa ngồi mềm điều hòa).
 */
@Slf4j
@Component("trainTicketStrategy")
public class TrainTicketStrategy implements TransportBookingStrategy {

    public static final String TYPE = "TRAIN_TICKET";

    @Override
    public String getTransportType() {
        return TYPE;
    }

    @Override
    public String getDisplayName() {
        return "Đường Sắt Du Lịch / Tàu Hỏa (Toa nằm mềm & Ngồi mềm)";
    }

    @Override
    public void validateBooking(TripEntity trip, SeatEntity seat, HoldSeatRequest request) {
        log.info("[Strategy - TrainTicket] Xác thực vé tàu hỏa: Chỗ {}, Chuyến Tàu ID: {}",
                seat.getSeatNumber(), trip.getId());

        // Vé tàu hỏa kiểm định thông tin căn cước công dân hoặc số điện thoại theo quy định đường sắt
        if (request.getCustomerPhone() == null || request.getCustomerPhone().isBlank()) {
            throw new IllegalArgumentException("Hành khách đi tàu hỏa bắt buộc phải cung cấp số điện thoại liên hệ xác thực!");
        }
    }

    @Override
    public BigDecimal calculateFare(TripEntity trip, SeatEntity seat, HoldSeatRequest request) {
        BigDecimal base = trip.getBasePrice() != null ? trip.getBasePrice() : BigDecimal.ZERO;
        // Phí bảo hiểm hành khách đường sắt bắt buộc: cộng 15.000 VNĐ
        return base.add(BigDecimal.valueOf(15000));
    }

    @Override
    public String getCancellationPolicy() {
        return "Vé tàu hỏa du lịch: Hủy vé trước 24h mất phí 10% theo quy định Tổng công ty Đường sắt Việt Nam; dưới 24h không hoàn vé.";
    }

    @Override
    public List<String> getBusinessRules() {
        return List.of(
                "Đã bao gồm bảo hiểm tai nạn đường sắt 15.000đ/vé",
                "Miễn phí 20kg hành lý ký gửi kèm vé tàu",
                "Trẻ em dưới 6 tuổi đi cùng người lớn được miễn vé (ngồi chung ghế)",
                "Xuất trình CCCD/Hộ chiếu trùng khớp tên trên vé khi lên tàu"
        );
    }
}
