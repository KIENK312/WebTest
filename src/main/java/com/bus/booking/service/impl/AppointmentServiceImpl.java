package com.bus.booking.service.impl;

import com.bus.booking.dto.request.AppointmentBookingRequest;
import com.bus.booking.dto.response.AppointmentResponseDto;
import com.bus.booking.entity.AppointmentEntity;
import com.bus.booking.enums.AppointmentStatus;
import com.bus.booking.exception.ResourceNotFoundException;
import com.bus.booking.repository.AppointmentRepository;
import com.bus.booking.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;

    public static final int MAX_APPOINTMENTS_PER_SLOT = 5;

    public static final List<String> STANDARD_TIME_SLOTS = List.of(
            "08:00 - 09:00",
            "09:15 - 10:15",
            "10:30 - 11:30",
            "13:30 - 14:30",
            "14:45 - 15:45",
            "16:00 - 17:00"
    );

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppointmentResponseDto scheduleAppointment(AppointmentBookingRequest request) {
        log.info("Xử lý đặt lịch hẹn tư vấn: khách={}, SĐT={}, ngày={}, slot={}",
                request.getCustomerName(), request.getCustomerPhone(),
                request.getAppointmentDate(), request.getTimeSlot());

        // 1. Kiểm tra ngày hẹn không nằm trong quá khứ
        if (request.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày hẹn tư vấn không thể ở trong quá khứ.");
        }

        // 2. Kiểm tra khung giờ hợp lệ
        if (!STANDARD_TIME_SLOTS.contains(request.getTimeSlot())) {
            throw new IllegalArgumentException("Khung giờ tư vấn '" + request.getTimeSlot() + "' không thuộc danh mục hỗ trợ.");
        }

        // 3. Kiểm tra số lượng lượt hẹn khả dụng tại văn phòng theo khung giờ (Capacity Limit)
        long currentCount = appointmentRepository.countActiveAppointmentsBySlot(
                request.getAppointmentDate(),
                request.getTimeSlot(),
                AppointmentStatus.CANCELLED
        );

        if (currentCount >= MAX_APPOINTMENTS_PER_SLOT) {
            throw new IllegalStateException(String.format(
                    "Khung giờ %s ngày %s đã đạt giới hạn tối đa (%d lượt hẹn). Quý khách vui lòng chọn khung giờ khác!",
                    request.getTimeSlot(), request.getAppointmentDate(), MAX_APPOINTMENTS_PER_SLOT
            ));
        }

        // 4. Sinh mã lịch hẹn tra cứu duy nhất: HEN-yyyyMMdd-XXXX
        String datePart = request.getAppointmentDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randPart = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String appointmentCode = "HEN-" + datePart + "-" + randPart;

        // 5. Khởi tạo thực thể lịch hẹn và lưu DB
        AppointmentEntity entity = AppointmentEntity.builder()
                .appointmentCode(appointmentCode)
                .customerName(request.getCustomerName().trim())
                .customerPhone(request.getCustomerPhone().trim())
                .customerEmail(request.getCustomerEmail() != null ? request.getCustomerEmail().trim() : null)
                .appointmentDate(request.getAppointmentDate())
                .timeSlot(request.getTimeSlot())
                .consultationType(request.getConsultationType())
                .status(AppointmentStatus.PENDING)
                .notes(request.getNotes())
                .assignedStaff("Chuyên viên tư vấn vé & tuyến")
                .build();

        AppointmentEntity saved = appointmentRepository.save(entity);
        log.info("Lưu thành công lịch hẹn tư vấn: ID={}, Mã={}", saved.getId(), saved.getAppointmentCode());

        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponseDto getAppointmentByCode(String appointmentCode) {
        return appointmentRepository.findByAppointmentCode(appointmentCode)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn với mã: " + appointmentCode));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDto> getAppointmentsByPhone(String phone) {
        return appointmentRepository.findByCustomerPhoneOrderByAppointmentDateDesc(phone)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDto> getAppointmentsByDate(LocalDate date) {
        return appointmentRepository.findByAppointmentDateOrderByTimeSlotAsc(date)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDto> getAllAppointments() {
        return appointmentRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppointmentResponseDto updateStatus(Long id, AppointmentStatus status, String assignedStaff) {
        AppointmentEntity entity = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn với ID: " + id));

        entity.setStatus(status);
        if (assignedStaff != null && !assignedStaff.isBlank()) {
            entity.setAssignedStaff(assignedStaff);
        }

        AppointmentEntity updated = appointmentRepository.save(entity);
        log.info("Cập nhật trạng thái lịch hẹn ID={} sang: {}", id, status);
        return mapToDto(updated);
    }

    @Override
    public List<String> getStandardTimeSlots() {
        return STANDARD_TIME_SLOTS;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSlotAvailable(LocalDate date, String timeSlot) {
        long currentCount = appointmentRepository.countActiveAppointmentsBySlot(
                date,
                timeSlot,
                AppointmentStatus.CANCELLED
        );
        return currentCount < MAX_APPOINTMENTS_PER_SLOT;
    }

    private AppointmentResponseDto mapToDto(AppointmentEntity entity) {
        return AppointmentResponseDto.builder()
                .id(entity.getId())
                .appointmentCode(entity.getAppointmentCode())
                .customerName(entity.getCustomerName())
                .customerPhone(entity.getCustomerPhone())
                .customerEmail(entity.getCustomerEmail())
                .appointmentDate(entity.getAppointmentDate())
                .timeSlot(entity.getTimeSlot())
                .consultationType(entity.getConsultationType())
                .status(entity.getStatus())
                .statusDescription(entity.getStatus().getDescription())
                .notes(entity.getNotes())
                .assignedStaff(entity.getAssignedStaff())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
