package com.bus.booking.enums;

/**
 * Trạng thái của Lịch hẹn tư vấn trực tiếp (Workflow 4 trong Đề án)
 */
public enum AppointmentStatus {
    PENDING("Chờ tiếp nhận"),
    CONFIRMED("Đã xác nhận lịch"),
    COMPLETED("Đã hoàn thành"),
    CANCELLED("Đã hủy");

    private final String description;

    AppointmentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
