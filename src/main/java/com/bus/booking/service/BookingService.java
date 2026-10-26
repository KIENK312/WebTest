package com.bus.booking.service;

import com.bus.booking.dto.request.HoldSeatRequest;
import com.bus.booking.dto.response.HoldSeatResponse;

public interface BookingService {

    /**
     * Giữ chỗ ghế ngồi cho hành khách:
     * Kiểm tra trạng thái ghế, tạo vé tạm thời (PENDING) và kiểm soát xung đột đồng thời với Optimistic Locking.
     */
    HoldSeatResponse holdSeat(HoldSeatRequest request);

    /**
     * Hủy vé/giữ chỗ và hoàn trả ghế lại trạng thái khả dụng.
     */
    HoldSeatResponse cancelTicket(Long ticketId);

    /**
     * Tra cứu chi tiết một vé xe theo ID.
     */
    HoldSeatResponse getTicketById(Long ticketId);

    /**
     * Lấy danh sách lịch sử vé xe của khách hàng theo userId hoặc số điện thoại.
     */
    java.util.List<HoldSeatResponse> getUserTickets(Long userId, String phone);
}
