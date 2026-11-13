package com.bus.booking.controller;

import com.bus.booking.dto.response.AdminDashboardResponseDto;
import com.bus.booking.dto.response.PartnerDto;
import com.bus.booking.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller Web MVC cho Phân hệ Quản trị viên (Admin Web Portal - Khối VI)
 * Hiển thị Dashboard phân tích số liệu thực tế từ SQL Server (KPI, Biểu đồ doanh thu 12 tháng, Quản lý Đối tác).
 */
@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class WebAdminController {

    private final AdminService adminService;

    /**
     * Bảng điều khiển tổng quan cho Quản Trị Viên (Admin Dashboard)
     */
    @GetMapping({"", "/dashboard"})
    public String showDashboard(Model model, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.isAuthenticated() && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            log.warn("[ADMIN-AUTH-CHECK] Người dùng không có quyền Admin cố gắng truy cập Dashboard: {}",
                    auth != null ? auth.getName() : "Anonymous");
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Yêu cầu quyền Quản Trị Viên: Vui lòng đăng nhập tài khoản ADMIN để truy cập Bảng điều khiển hệ thống.");
            return "redirect:/login";
        }

        AdminDashboardResponseDto dashboard = adminService.getDashboardOverview();
        List<PartnerDto> partners = adminService.getAllPartners();

        model.addAttribute("dashboard", dashboard);
        model.addAttribute("partners", partners);
        model.addAttribute("currentUsername", auth.getName());

        return "admin/dashboard";
    }

    /**
     * Cập nhật trạng thái đối tác nhà xe (ACTIVE / SUSPENDED / INACTIVE)
     * Áp dụng PRG Pattern: POST -> Redirect 303 về /admin/dashboard
     */
    @PostMapping("/partners/{id}/status")
    public String updatePartnerStatus(
            @PathVariable("id") Long partnerId,
            @RequestParam("status") String status,
            RedirectAttributes redirectAttributes) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.isAuthenticated() && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            redirectAttributes.addFlashAttribute("errorMessage", "Từ chối thao tác: Bạn không có quyền Admin!");
            return "redirect:/login";
        }

        try {
            adminService.updatePartnerStatus(partnerId, status);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cập nhật trạng thái nhà xe thành công: " + status);
        } catch (Exception ex) {
            log.error("Lỗi cập nhật đối tác ID={}: {}", partnerId, ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/dashboard";
    }

    /**
     * Thêm đối tác nhà xe mới vào hệ thống
     * Áp dụng PRG Pattern: POST -> Redirect 303 về /admin/dashboard
     */
    @PostMapping("/partners")
    public String createPartner(
            @ModelAttribute PartnerDto partnerRequest,
            RedirectAttributes redirectAttributes) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.isAuthenticated() && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            redirectAttributes.addFlashAttribute("errorMessage", "Từ chối thao tác: Bạn không có quyền Admin!");
            return "redirect:/login";
        }

        try {
            adminService.createPartner(partnerRequest);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đăng ký nhà xe đối tác mới thành công: " + partnerRequest.getName());
        } catch (Exception ex) {
            log.error("Lỗi thêm mới đối tác: {}", ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/dashboard";
    }
}
