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
     * Tra cứu lịch hẹn theo số điện thoại hoặc mã hẹn
     */
    @GetMapping("/lookup")
    public String lookupAppointments(
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "code", required = false) String code,
            Model model) {

        if (code != null && !code.isBlank()) {
            try {
                AppointmentResponseDto item = appointmentService.getAppointmentByCode(code.trim());
                model.addAttribute("appointments", List.of(item));
            } catch (Exception e) {
                model.addAttribute("errorMessage", "Không tìm thấy lịch hẹn với mã: " + code);
            }
        } else if (phone != null && !phone.isBlank()) {
            List<AppointmentResponseDto> list = appointmentService.getAppointmentsByPhone(phone.trim());
            model.addAttribute("appointments", list);
            if (list.isEmpty()) {
                model.addAttribute("infoMessage", "Chưa có lịch hẹn nào gắn với số điện thoại: " + phone);
            }
        }
        return "appointments/lookup";
    }
}
