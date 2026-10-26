package com.bus.booking.service;

import com.bus.booking.entity.TicketEntity;

public interface NotificationService {

    /**
     * Gửi email bất đồng bộ (@Async) thông báo xác nhận đặt vé thành công kèm mã vé.
     */
    void sendBookingConfirmationEmail(TicketEntity ticket);
}
