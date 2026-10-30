package com.bus.booking.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {

    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @JsonAlias({"phone", "phoneNumber"})
    private String phoneNumber;

    // Mật khẩu mới (tùy chọn: chỉ cập nhật nếu người dùng nhập)
    private String password;

    @JsonProperty("phone")
    public void setPhone(String phone) {
        if (phone != null && !phone.isBlank()) {
            this.phoneNumber = phone.trim();
        }
    }
}
