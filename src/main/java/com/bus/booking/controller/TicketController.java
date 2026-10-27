package com.bus.booking.controller;

import com.bus.booking.dto.request.HoldSeatRequest;
import com.bus.booking.dto.response.ApiResponse;
import com.bus.booking.dto.response.HoldSeatResponse;
import com.bus.booking.security.CustomUserDetails;
import com.bus.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final BookingService bookingService;

    /**
     * Giữ chỗ ghế ngồi cho hành khách trong 15 phút.
     * Sử dụng Optimistic Locking (@Version) chống trùng ghế đồng thời.
     */
    @PostMapping("/hold")
    public ResponseEntity<ApiResponse<HoldSeatResponse>> holdSeat(
            @Valid @RequestBody HoldSeatRequest request) {
        HoldSeatResponse response = bookingService.holdSeat(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Giữ chỗ thành công trong 15 phút", response));
    }

    /**
     * Hủy vé hoặc hủy giữ chỗ, hoàn trả lại ghế cho hệ thống.
     */
    @PutMapping("/cancel/{id}")
    public ResponseEntity<ApiResponse<HoldSeatResponse>> cancelTicket(
            @PathVariable("id") Long ticketId) {
        HoldSeatResponse response = bookingService.cancelTicket(ticketId);
        return ResponseEntity.ok(ApiResponse.success("Hủy vé thành công", response));
    }

    /**
     * Tra cứu chi tiết vé xe theo ID (phục vụ kiểm tra trạng thái thanh toán và mã QR vé).
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HoldSeatResponse>> getTicketById(
            @PathVariable("id") Long ticketId) {
        HoldSeatResponse response = bookingService.getTicketById(ticketId);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin vé thành công", response));
    }

    /**
     * BẢO MẬT: Lấy danh sách lịch sử vé của người dùng đang đăng nhập qua JWT Token.
     * Trích xuất userId trực tiếp từ SecurityContextHolder (CustomUserDetails), tuyệt đối không dùng findAll().
     * Nếu là khách vãng lai chưa đăng nhập, cho phép tra cứu an toàn theo số điện thoại (phone).
     */
    @GetMapping("/my-tickets")
    public ResponseEntity<ApiResponse<java.util.List<HoldSeatResponse>>> getMyTickets(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "phone", required = false) String phone) {
        Long userId = (userDetails != null) ? userDetails.getId() : null;
        java.util.List<HoldSeatResponse> list = bookingService.getUserTickets(userId, phone);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách vé thành công", list));
    }

    /**
     * Endpoint tương thích: Lấy danh sách lịch sử vé đã đặt.
     */
    @GetMapping("/my-history")
    public ResponseEntity<ApiResponse<java.util.List<HoldSeatResponse>>> getMyHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "userId", required = false) Long paramUserId,
            @RequestParam(value = "phone", required = false) String phone) {
        // Ưu tiên userId từ JWT Token đã xác thực
        Long effectiveUserId = (userDetails != null) ? userDetails.getId() : paramUserId;
        java.util.List<HoldSeatResponse> list = bookingService.getUserTickets(effectiveUserId, phone);
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử vé thành công", list));
    }
}
