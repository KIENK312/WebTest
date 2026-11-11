package com.bus.booking.controller;

import com.bus.booking.dto.request.AppointmentBookingRequest;
import com.bus.booking.dto.response.ApiResponse;
import com.bus.booking.dto.response.AppointmentResponseDto;
import com.bus.booking.enums.AppointmentStatus;
import com.bus.booking.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller cho phân hệ Đặt lịch hẹn tư vấn trực tiếp (Workflow 4)
 */
@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    /**
     * Khách hàng / Đại lý gửi yêu cầu đặt lịch hẹn mới
     */
    @PostMapping("/schedule")
    public ResponseEntity<ApiResponse<AppointmentResponseDto>> scheduleAppointment(
            @Valid @RequestBody AppointmentBookingRequest request) {
        AppointmentResponseDto response = appointmentService.scheduleAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Đăng ký lịch hẹn tư vấn thành công", response));
    }

    /**
     * Tra cứu chi tiết lịch hẹn theo mã hẹn (HEN-...)
     */
    @GetMapping("/{code}")
    public ResponseEntity<ApiResponse<AppointmentResponseDto>> getAppointmentByCode(
            @PathVariable("code") String code) {
        AppointmentResponseDto response = appointmentService.getAppointmentByCode(code);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin lịch hẹn thành công", response));
    }

    /**
     * Tra cứu lịch hẹn của khách hàng theo số điện thoại
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<AppointmentResponseDto>>> getMyAppointments(
            @RequestParam("phone") String phone) {
        List<AppointmentResponseDto> list = appointmentService.getAppointmentsByPhone(phone);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách lịch hẹn thành công", list));
    }

    /**
     * Danh sách khung giờ chuẩn được hỗ trợ
     */
    @GetMapping("/standard-slots")
    public ResponseEntity<ApiResponse<List<String>>> getStandardSlots() {
        return ResponseEntity.ok(ApiResponse.success("Danh sách khung giờ tư vấn", appointmentService.getStandardTimeSlots()));
    }

    /**
     * Nhân viên nội bộ (OPERATOR, ADMIN): Lấy toàn bộ lịch hẹn
     */
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<AppointmentResponseDto>>> getAllAppointments() {
        List<AppointmentResponseDto> list = appointmentService.getAllAppointments();
        return ResponseEntity.ok(ApiResponse.success("Lấy toàn bộ lịch hẹn thành công", list));
    }

    /**
     * Nhân viên nội bộ (OPERATOR, ADMIN): Lọc lịch hẹn theo ngày
     */
    @GetMapping("/by-date")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<AppointmentResponseDto>>> getAppointmentsByDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AppointmentResponseDto> list = appointmentService.getAppointmentsByDate(date);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách lịch hẹn theo ngày thành công", list));
    }

    /**
     * Nhân viên nội bộ (OPERATOR, ADMIN): Cập nhật trạng thái lịch hẹn
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<AppointmentResponseDto>> updateStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") AppointmentStatus status,
            @RequestParam(value = "assignedStaff", required = false) String assignedStaff) {
        AppointmentResponseDto response = appointmentService.updateStatus(id, status, assignedStaff);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái lịch hẹn thành công", response));
    }
}
