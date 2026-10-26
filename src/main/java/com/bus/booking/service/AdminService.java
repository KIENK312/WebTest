package com.bus.booking.service;

import com.bus.booking.dto.response.AdminDashboardResponseDto;
import com.bus.booking.dto.response.PartnerDto;

import java.util.List;

public interface AdminService {

    /**
     * Lấy dữ liệu thống kê tổng quan thực tế từ SQL Server (100% realtime, không mock).
     */
    AdminDashboardResponseDto getDashboardOverview();

    /**
     * Lấy danh sách toàn bộ đối tác nhà xe (PartnerDto).
     */
    List<PartnerDto> getAllPartners();

    /**
     * Cập nhật trạng thái đối tác nhà xe (ACTIVE, SUSPENDED, INACTIVE).
     */
    PartnerDto updatePartnerStatus(Long partnerId, String status);

    /**
     * Thêm mới đối tác nhà xe vào hệ thống.
     */
    PartnerDto createPartner(PartnerDto request);
}
