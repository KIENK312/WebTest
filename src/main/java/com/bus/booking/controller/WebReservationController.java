package com.bus.booking.controller;

import com.bus.booking.dto.request.HoldSeatRequest;
import com.bus.booking.dto.request.PaymentWebhookRequest;
import com.bus.booking.dto.response.HoldSeatResponse;
import com.bus.booking.dto.response.PaymentQrResponse;
import com.bus.booking.enums.TicketStatus;
import com.bus.booking.service.BookingService;
import com.bus.booking.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Controller triển khai mô hình POST/REDIRECT/GET (PRG Pattern) chuẩn mực
 * theo yêu cầu Khối V và Workflow 1 (End-to-End Reservation & PRG Workflow).
 * Chống trùng lặp giao dịch và ngăn chặn việc bấm Refresh (F5) sinh thêm lệnh giữ chỗ/thanh toán.
 */
@Slf4j
@Controller
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class WebReservationController {

    private final BookingService bookingService;
    private final PaymentService paymentService;

    /**
     * BƯỚC 1 (POST): Tiếp nhận biểu mẫu giữ chỗ vé du lịch
     * Áp dụng PRG: Sau khi xử lý xong chuyển hướng (HTTP 303 Redirect) sang endpoint GET payment-step.
     */
    @PostMapping("/book")
    public String bookTicket(
            @Valid @ModelAttribute("bookingRequest") HoldSeatRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        log.info("[PRG-STEP-1: POST] Nhận yêu cầu đặt vé: chuyến={}, ghế={}, khách={}",
                request.getTripId(), request.getSeatId(), request.getCustomerName());

        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("errorMessage", errorMsg);
            return "redirect:/trips/" + request.getTripId() + "/booking";
        }

        try {
            HoldSeatResponse response = bookingService.holdSeat(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Giữ chỗ thành công! Vui lòng hoàn tất thanh toán trong 15 phút.");

            // PRG Pattern: Trả về HTTP 303 Redirect sang GET /reservations/{id}/payment-step
            return "redirect:/reservations/" + response.getTicketId() + "/payment-step";

        } catch (Exception ex) {
            log.error("[PRG-STEP-1: ERROR] Lỗi khi giữ chỗ: {}", ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/trips/" + request.getTripId() + "/booking";
        }
    }

    /**
     * BƯỚC 2 (GET): Hiển thị hóa đơn tạm tính & mã thanh toán VietQR (Chống F5 gây duplicate booking)
     */
    @GetMapping("/{id}/payment-step")
    public String showPaymentStep(
            @PathVariable("id") Long ticketId,
            Model model,
            RedirectAttributes redirectAttributes) {

        log.info("[PRG-STEP-2: GET] Truy cập trang thanh toán vé ID: {}", ticketId);

        try {
            HoldSeatResponse ticket = bookingService.getTicketById(ticketId);
            PaymentQrResponse qr = paymentService.createPaymentQr(ticketId);

            model.addAttribute("ticket", ticket);
            model.addAttribute("qr", qr);
            return "reservations/payment-step";

        } catch (Exception ex) {
            log.error("[PRG-STEP-2: ERROR] Không tìm thấy vé ID={}: {}", ticketId, ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Vé không tồn tại hoặc đã hết hạn giữ chỗ.");
            return "redirect:/trips";
        }
    }

    /**
     * BƯỚC 3 (POST): Tiếp nhận xác nhận thanh toán giao dịch đặt vé
     * Áp dụng PRG: Sau khi ghi nhận giao dịch thành công, Redirect sang GET confirmation.
     */
    @PostMapping("/{id}/pay")
    public String processPayment(
            @PathVariable("id") Long ticketId,
            RedirectAttributes redirectAttributes) {

        log.info("[PRG-STEP-3: POST] Xử lý xác nhận thanh toán vé ID: {}", ticketId);

        try {
            HoldSeatResponse ticket = bookingService.getTicketById(ticketId);

            if (ticket.getStatus() != TicketStatus.CONFIRMED) {
                // Giả lập giao dịch thanh toán thành công qua cổng thanh toán / ngân hàng
                PaymentWebhookRequest webhookRequest = PaymentWebhookRequest.builder()
                        .gateway("VIETQR_DEMO")
                        .transactionCode("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                        .transferContent("VE" + ticketId)
                        .transferAmount(ticket.getPrice())
                        .transactionDate(LocalDateTime.now())
                        .build();

                paymentService.processWebhook(webhookRequest);
            }

            redirectAttributes.addFlashAttribute("paymentSuccess", true);
            // PRG Pattern: Redirect sang GET /reservations/{id}/confirmation
            return "redirect:/reservations/" + ticketId + "/confirmation";

        } catch (Exception ex) {
            log.error("[PRG-STEP-3: ERROR] Lỗi thanh toán vé ID={}: {}", ticketId, ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Thanh toán thất bại: " + ex.getMessage());
            return "redirect:/reservations/" + ticketId + "/payment-step";
        }
    }

    /**
     * BƯỚC 4 (GET): Hiển thị Vé điện tử / Tem số / E-Ticket & QR Code đã kích hoạt
     * Ngăn chặn F5 làm phát sinh thêm lệnh trừ tiền hay giao dịch trùng lặp.
     */
    @GetMapping("/{id}/confirmation")
    public String showConfirmation(
            @PathVariable("id") Long ticketId,
            Model model,
            RedirectAttributes redirectAttributes) {

        log.info("[PRG-STEP-4: GET] Hiển thị kết quả vé điện tử ID: {}", ticketId);

        try {
            HoldSeatResponse ticket = bookingService.getTicketById(ticketId);
            model.addAttribute("ticket", ticket);
            return "reservations/confirmation";

        } catch (Exception ex) {
            log.error("[PRG-STEP-4: ERROR] Không tìm thấy vé: {}", ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể lấy thông tin vé.");
            return "redirect:/trips";
        }
    }
}
