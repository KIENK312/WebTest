package com.bus.booking.entity;

import com.bus.booking.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Thực thể Lịch hẹn tư vấn trực tiếp (Appointment Entity)
 * Phục vụ Workflow 4: Appointment Consultation trong Kiến trúc hệ thống
 */
@Entity
@Table(name = "appointments", indexes = {
        @Index(name = "idx_appointments_phone", columnList = "customer_phone"),
        @Index(name = "idx_appointments_code", columnList = "appointment_code"),
        @Index(name = "idx_appointments_date_slot", columnList = "appointment_date, time_slot")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "appointment_code", nullable = false, unique = true, length = 50)
    private String appointmentCode;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "customer_phone", nullable = false, length = 20)
    private String customerPhone;

    @Column(name = "customer_email", length = 100)
    private String customerEmail;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "time_slot", nullable = false, length = 50)
    private String timeSlot;

    @Column(name = "consultation_type", nullable = false, length = 100)
    private String consultationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.PENDING;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "assigned_staff", length = 100)
    private String assignedStaff;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
