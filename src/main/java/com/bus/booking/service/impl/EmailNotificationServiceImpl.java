package com.bus.booking.service.impl;

import com.bus.booking.entity.TicketEntity;
import com.bus.booking.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailNotificationServiceImpl implements NotificationService {

    @Async("mailTaskExecutor")
    @Override
    public void sendBookingConfirmationEmail(TicketEntity ticket) {
        String recipient = ticket.getCustomerEmail();
        if (recipient == null || recipient.isBlank()) {
            log.warn("[ASYNC-EMAIL] Vé id={} không có địa chỉ email để gửi thông báo", ticket.getId());
            return;
        }

        log.info("[ASYNC-EMAIL] Đang bắt đầu tiến trình gửi email xác nhận đặt vé tới: {}", recipient);
        try {
            // Mô phỏng quá trình kết nối và gửi mail qua SMTP server
            Thread.sleep(150);

            log.info("----------------------------------------------------------------------");
            log.info(">>> GỬI EMAIL THÀNH CÔNG ĐẾN: {}", recipient);
            log.info(">>> Kính gửi quý khách: {}", ticket.getCustomerName());
            log.info(">>> MÃ VÉ ĐIỆN TỬ: {}", ticket.getTicketCode());
            log.info(">>> Tuyến đường: {} -> {}",
                    ticket.getTrip().getRoute().getOrigin(),
                    ticket.getTrip().getRoute().getDestination());
            log.info(">>> Giờ xuất bến: {}", ticket.getTrip().getDepartureTime());
            log.info(">>> Vị trí ghế: {} (Tầng {})",
                    ticket.getSeat().getSeatNumber(),
                    ticket.getSeat().getFloorNumber());
            log.info(">>> Số tiền thanh toán: {} VNĐ", ticket.getPrice());
            log.info(">>> Trạng thái vé: ĐÃ XÁC NHẬN (CONFIRMED)");
            log.info("----------------------------------------------------------------------");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[ASYNC-EMAIL] Tiến trình gửi email bị gián đoạn: {}", e.getMessage());
        }
    }
}
