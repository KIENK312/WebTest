package com.bus.booking.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 4, max = 50, message = "Tên đăng nhập phải từ 4 đến 50 ký tự")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @JsonAlias({"phone", "phoneNumber"})
    private String phoneNumber;

    // Tùy chọn vai trò: ROLE_CUSTOMER, ROLE_OPERATOR (mặc định ROLE_CUSTOMER)
    private String roleName;

    // ID nhà xe nếu là nhân viên nhà xe
    private Long operatorId;

    /**
     * Tương thích linh hoạt cả trường 'phone' lẫn 'phoneNumber' từ Frontend JSON payload
     */
    @JsonProperty("phone")
    public void setPhone(String phone) {
        if (phone != null && !phone.isBlank()) {
            this.phoneNumber = phone.trim();
        }
    }

    public String getPhone() {
        return this.phoneNumber;
    }
}
