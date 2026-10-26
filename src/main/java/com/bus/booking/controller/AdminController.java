package com.bus.booking.controller;

import com.bus.booking.dto.response.AdminDashboardResponseDto;
import com.bus.booking.dto.response.ApiResponse;
import com.bus.booking.dto.response.PartnerDto;
import com.bus.booking.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * Bảng điều khiển tổng quan cho Quản trị viên (Admin Overview/Dashboard):
     * Dữ liệu thống kê doanh thu thực tế từ SQL Server (100% realtime, không mock).
     */
    @GetMapping({"/overview", "/dashboard", "/stats"})
    public ResponseEntity<ApiResponse<AdminDashboardResponseDto>> getOverview() {
        AdminDashboardResponseDto data = adminService.getDashboardOverview();
        return ResponseEntity.ok(ApiResponse.success("Lấy số liệu tổng quan thành công", data));
    }

    /**
     * Danh sách nhà xe đối tác (PartnerDto).
     */
    @GetMapping("/partners")
    public ResponseEntity<ApiResponse<List<PartnerDto>>> getPartners() {
        List<PartnerDto> partners = adminService.getAllPartners();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách nhà xe đối tác thành công", partners));
    }

    /**
     * Phê duyệt hoặc cập nhật trạng thái đối tác nhà xe (ACTIVE, SUSPENDED, INACTIVE).
     */
    @PutMapping("/partners/{id}/status")
    public ResponseEntity<ApiResponse<PartnerDto>> updatePartnerStatus(
            @PathVariable("id") Long partnerId,
            @RequestParam("status") String statusStr) {
        PartnerDto updated = adminService.updatePartnerStatus(partnerId, statusStr);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái đối tác thành công", updated));
    }

    /**
     * Thêm nhà xe đối tác mới vào hệ thống.
     */
    @PostMapping("/partners")
    public ResponseEntity<ApiResponse<PartnerDto>> createPartner(
            @RequestBody PartnerDto request) {
        PartnerDto saved = adminService.createPartner(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Đăng ký đối tác nhà xe mới thành công", saved));
    }
}
