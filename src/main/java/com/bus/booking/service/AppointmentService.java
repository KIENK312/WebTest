package com.bus.booking.service;

import com.bus.booking.dto.request.AppointmentBookingRequest;
import com.bus.booking.dto.response.AppointmentResponseDto;
import com.bus.booking.enums.AppointmentStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * Service xử lý nghiệp vụ Lịch hẹn tư vấn (Workflow 4: Appointment Consultation)
 */
public interface AppointmentService {

    /**
     * Đặt lịch hẹn tư vấn mới
     * Kiểm tra số lượng lượt hẹn khả dụng theo khung giờ và ngày làm việc
     */
    AppointmentResponseDto scheduleAppointment(AppointmentBookingRequest request);

    /**
     * Tra cứu chi tiết lịch hẹn theo mã hẹn (HEN-...)
     */
    AppointmentResponseDto getAppointmentByCode(String appointmentCode);

    /**
     * Lấy danh sách lịch hẹn của khách hàng theo số điện thoại
     */
    List<AppointmentResponseDto> getAppointmentsByPhone(String phone);

    /**
     * Lấy danh sách lịch hẹn trong ngày (phục vụ nhân viên điều hành / tư vấn)
     */
    List<AppointmentResponseDto> getAppointmentsByDate(LocalDate date);

    /**
     * Lấy toàn bộ danh sách lịch hẹn
     */
    List<AppointmentResponseDto> getAllAppointments();

    /**
     * Cập nhật trạng thái lịch hẹn và phân công nhân viên tư vấn
     */
    AppointmentResponseDto updateStatus(Long id, AppointmentStatus status, String assignedStaff);

    /**
     * Lấy danh sách các khung giờ và số chỗ còn lại trong ngày
     */
    List<String> getStandardTimeSlots();

    /**
     * Kiểm tra khung giờ còn chỗ hay không (tối đa 5 lượt/slot)
     */
    boolean isSlotAvailable(LocalDate date, String timeSlot);
}
