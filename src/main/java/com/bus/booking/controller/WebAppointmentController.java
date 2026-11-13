package com.bus.booking.controller;

import com.bus.booking.dto.request.AppointmentBookingRequest;
import com.bus.booking.dto.response.AppointmentResponseDto;
import com.bus.booking.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller Web MVC cho phân hệ Lịch hẹn tư vấn trực tiếp (Workflow 4)
 * Hiện thực hóa mô hình PRG (Post/Redirect/Get) chống gửi trùng form đăng ký.
 */
@Slf4j
@Controller
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class WebAppointmentController {

    private final AppointmentService appointmentService;

    /**
     * Hiển thị biểu mẫu Đăng ký lịch hẹn tư vấn
     */
    @GetMapping
    public String showAppointmentForm(Model model) {
        if (!model.containsAttribute("appointmentRequest")) {
            model.addAttribute("appointmentRequest", AppointmentBookingRequest.builder()
                    .appointmentDate(LocalDate.now().plusDays(1))
                    .timeSlot("09:15 - 10:15")
                    .consultationType("Tư vấn lộ trình & đặt vé đoàn")
                    .build());
        }
        model.addAttribute("timeSlots", appointmentService.getStandardTimeSlots());
        return "appointments/schedule";
    }

    /**
     * Tiếp nhận biểu mẫu Đăng ký lịch hẹn qua POST và Redirect (PRG Pattern)
     */
    @PostMapping("/schedule")
    public String handleSchedule(
            @Valid @ModelAttribute("appointmentRequest") AppointmentBookingRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        log.info("[APPOINTMENT-PRG: POST] Tiếp nhận đăng ký hẹn: khách={}, ngày={}, slot={}",
                request.getCustomerName(), request.getAppointmentDate(), request.getTimeSlot());

        if (bindingResult.hasErrors()) {
            String error = bindingResult.getAllErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("errorMessage", error);
            redirectAttributes.addFlashAttribute("appointmentRequest", request);
            return "redirect:/appointments";
        }

        try {
            AppointmentResponseDto response = appointmentService.scheduleAppointment(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đăng ký lịch hẹn tư vấn thành công! Chúng tôi đã gửi thông báo đến chuyên viên phụ trách.");

            // PRG Pattern: Redirect HTTP 303 sang trang xác nhận GET
            return "redirect:/appointments/" + response.getAppointmentCode() + "/confirmation";

        } catch (Exception ex) {
            log.error("[APPOINTMENT-PRG: ERROR] Lỗi đăng ký hẹn: {}", ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("appointmentRequest", request);
            return "redirect:/appointments";
        }
    }

    /**
     * Hiển thị trang kết quả xác nhận lịch hẹn (GET)
     * Ngăn chặn người dùng F5 nộp lại form đăng ký
     */
    @GetMapping("/{code}/confirmation")
    public String showConfirmation(
            @PathVariable("code") String code,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            AppointmentResponseDto appointment = appointmentService.getAppointmentByCode(code);
            model.addAttribute("appointment", appointment);
            return "appointments/confirmation";

        } catch (Exception ex) {
            log.error("Không tìm thấy mã lịch hẹn {}: {}", code, ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy thông tin lịch hẹn.");
            return "redirect:/appointments";
        }
    }

    /**
     * Tra cứu lịch hẹn:
     * - Khách hàng: Chỉ được tra cứu Tem Số của chính mình (nhập Mã hẹn HEN-... hoặc Số điện thoại).
     *   Mặc định không hiển thị danh sách của người khác để đảm bảo bảo mật thông tin.
     * - Admin / Operator: Được tra cứu và xem danh sách tiếp nhận tại quầy khi truyền all=true.
     */
    @GetMapping("/lookup")
    public String lookupAppointments(
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "all", required = false, defaultValue = "false") boolean showAll,
            Model model) {

        if (code != null && !code.isBlank()) {
            try {
                AppointmentResponseDto item = appointmentService.getAppointmentByCode(code.trim());
                model.addAttribute("appointments", List.of(item));
                model.addAttribute("searchedCode", code.trim());
            } catch (Exception e) {
                model.addAttribute("errorMessage", "Không tìm thấy tem số với mã: " + code);
            }
        } else if (phone != null && !phone.isBlank()) {
            List<AppointmentResponseDto> list = appointmentService.getAppointmentsByPhone(phone.trim());
            model.addAttribute("appointments", list);
            model.addAttribute("searchedPhone", phone.trim());
            if (list.isEmpty()) {
                model.addAttribute("infoMessage", "Chưa có tem số nào gắn với số điện thoại: " + phone);
            }
        } else if (showAll) {
            // Chỉ dành cho nhân viên điều hành / Admin xem bàn tiếp nhận tại quầy
            List<AppointmentResponseDto> list = appointmentService.getAllAppointments();
            model.addAttribute("appointments", list);
            model.addAttribute("showAll", true);
        } else {
            // Mặc định với khách hàng: Không hiển thị tem số của người khác!
            model.addAttribute("appointments", null);
            model.addAttribute("promptForSearch", true);
        }
        return "appointments/lookup";
    }

    /**
     * Cập nhật trạng thái lịch hẹn / tem số tư vấn (Dành cho Admin & Operator trực quầy)
     * Áp dụng PRG: Sau khi update, HTTP 303 Redirect về trang lookup.
     */
    @PostMapping("/{id}/status")
    public String updateAppointmentStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") com.bus.booking.enums.AppointmentStatus newStatus,
            @RequestParam(value = "assignedStaff", required = false) String assignedStaff,
            @RequestParam(value = "returnCode", required = false) String returnCode,
            RedirectAttributes redirectAttributes) {

        try {
            appointmentService.updateStatus(id, newStatus, assignedStaff);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cập nhật trạng thái lịch hẹn thành công: " + newStatus.name());
        } catch (Exception ex) {
            log.error("[APPOINTMENT-STATUS-UPDATE: ERROR] Lỗi cập nhật trạng thái: {}", ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        if (returnCode != null && !returnCode.isBlank()) {
            return "redirect:/appointments/lookup?code=" + returnCode;
        }
        return "redirect:/appointments/lookup";
    }
}
