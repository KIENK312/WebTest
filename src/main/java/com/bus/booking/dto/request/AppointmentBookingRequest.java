package com.bus.booking.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentBookingRequest {

    @NotBlank(message = "Họ và tên khách hàng không được để trống")
    private String customerName;

    @NotBlank(message = "Số điện thoại liên hệ không được để trống")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ (10 số, bắt đầu bằng 0 hoặc +84)")
    private String customerPhone;

    private String customerEmail;

    @NotNull(message = "Ngày hẹn tư vấn không được để trống")
    @FutureOrPresent(message = "Ngày hẹn phải từ ngày hôm nay trở đi")
    private LocalDate appointmentDate;

    @NotBlank(message = "Khung giờ hẹn không được để trống")
    private String timeSlot;

    @NotBlank(message = "Vui lòng chọn nội dung cần tư vấn")
    private String consultationType;

    private String notes;
}
