package com.bus.booking.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoldSeatRequest {

    @NotNull(message = "Mã chuyến đi (tripId) không được để trống")
    private Long tripId;

    @NotNull(message = "Mã ghế (seatId) không được để trống")
    private Long seatId;

    private Long userId;

    @NotBlank(message = "Tên hành khách không được để trống")
    private String customerName;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String customerPhone;

    @Email(message = "Email không đúng định dạng")
    private String customerEmail;
}
