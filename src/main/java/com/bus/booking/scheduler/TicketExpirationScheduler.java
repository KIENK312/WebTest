package com.bus.booking.scheduler;

import com.bus.booking.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Background Scheduler: Tự động quét và giải phóng các ghế ngồi bị giữ chỗ quá 10 phút
 * mà khách hàng chưa hoàn tất thanh toán.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TicketExpirationScheduler {

    private final TicketRepository ticketRepository;

    /**
     * Chạy định kỳ mỗi 60 giây (60.000 ms).
     * Ngưỡng hết hạn giữ chỗ: 10 phút tính từ thời điểm đặt (bookingTime).
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void sweepExpiredHoldTickets() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(10);
        int expiredCount = ticketRepository.expireOldPendingTickets(cutoff);

        if (expiredCount > 0) {
            log.info("⏰ [SCHEDULED-WORKER] Đã tự động thu hồi và giải phóng {} ghế ngồi bị giữ chỗ quá 10 phút (cutoff: {})",
                    expiredCount, cutoff);
        }
    }
}
