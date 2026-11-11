package com.bus.booking.service;

import com.bus.booking.dto.request.AppointmentBookingRequest;
import com.bus.booking.dto.response.AppointmentResponseDto;
import com.bus.booking.entity.AppointmentEntity;
import com.bus.booking.enums.AppointmentStatus;
import com.bus.booking.exception.ResourceNotFoundException;
import com.bus.booking.repository.AppointmentRepository;
import com.bus.booking.service.impl.AppointmentServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    @Test
    @DisplayName("Đặt lịch hẹn tư vấn thành công khi thông tin hợp lệ và slot còn chỗ")
    void scheduleAppointment_Success() {
        AppointmentBookingRequest request = AppointmentBookingRequest.builder()
                .customerName("Trần Văn Nam")
                .customerPhone("0988776655")
                .customerEmail("namtran@example.com")
                .appointmentDate(LocalDate.now().plusDays(2))
                .timeSlot("09:15 - 10:15")
                .consultationType("Tư vấn lộ trình & đặt vé đoàn")
                .notes("Đoàn 30 người")
                .build();

        when(appointmentRepository.countActiveAppointmentsBySlot(any(), any(), any())).thenReturn(2L);
        when(appointmentRepository.save(any(AppointmentEntity.class))).thenAnswer(invocation -> {
            AppointmentEntity entity = invocation.getArgument(0);
            entity.setId(10L);
            return entity;
        });

        AppointmentResponseDto response = appointmentService.scheduleAppointment(request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertTrue(response.getAppointmentCode().startsWith("HEN-"));
        assertEquals("Trần Văn Nam", response.getCustomerName());
        assertEquals(AppointmentStatus.PENDING, response.getStatus());

        verify(appointmentRepository, times(1)).save(any(AppointmentEntity.class));
    }

    @Test
    @DisplayName("Ném ngoại lệ khi ngày hẹn tư vấn nằm trong quá khứ")
    void scheduleAppointment_PastDate_ThrowsException() {
        AppointmentBookingRequest request = AppointmentBookingRequest.builder()
                .customerName("Lê Văn B")
                .customerPhone("0901234567")
                .appointmentDate(LocalDate.now().minusDays(1))
                .timeSlot("09:15 - 10:15")
                .consultationType("Tư vấn đặt vé")
                .build();

        assertThrows(IllegalArgumentException.class, () -> appointmentService.scheduleAppointment(request));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ném ngoại lệ khi khung giờ hẹn đã đạt giới hạn tối đa 5 lượt")
    void scheduleAppointment_SlotFull_ThrowsException() {
        AppointmentBookingRequest request = AppointmentBookingRequest.builder()
                .customerName("Hoàng Văn C")
                .customerPhone("0909998877")
                .appointmentDate(LocalDate.now().plusDays(1))
                .timeSlot("09:15 - 10:15")
                .consultationType("Tư vấn hợp đồng")
                .build();

        when(appointmentRepository.countActiveAppointmentsBySlot(any(), any(), any())).thenReturn(5L);

        assertThrows(IllegalStateException.class, () -> appointmentService.scheduleAppointment(request));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cập nhật trạng thái lịch hẹn và phân công nhân viên tư vấn thành công")
    void updateStatus_Success() {
        AppointmentEntity entity = AppointmentEntity.builder()
                .id(1L)
                .appointmentCode("HEN-TEST-01")
                .customerName("Khách Hàng")
                .customerPhone("0901234567")
                .appointmentDate(LocalDate.now().plusDays(1))
                .timeSlot("08:00 - 09:00")
                .consultationType("Tư vấn lộ trình")
                .status(AppointmentStatus.PENDING)
                .build();

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(appointmentRepository.save(any(AppointmentEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        AppointmentResponseDto response = appointmentService.updateStatus(1L, AppointmentStatus.CONFIRMED, "Nguyễn Văn Chuyên Viên");

        assertNotNull(response);
        assertEquals(AppointmentStatus.CONFIRMED, response.getStatus());
        assertEquals("Nguyễn Văn Chuyên Viên", response.getAssignedStaff());
    }

    @Test
    @DisplayName("Ném ngoại lệ ResourceNotFoundException khi tra cứu mã hẹn không tồn tại")
    void getAppointmentByCode_NotFound_ThrowsException() {
        when(appointmentRepository.findByAppointmentCode("HEN-NOTFOUND")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> appointmentService.getAppointmentByCode("HEN-NOTFOUND"));
    }
}
