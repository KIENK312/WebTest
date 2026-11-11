package com.bus.booking.repository;

import com.bus.booking.entity.AppointmentEntity;
import com.bus.booking.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentEntity, Long> {

    Optional<AppointmentEntity> findByAppointmentCode(String appointmentCode);

    List<AppointmentEntity> findByCustomerPhoneOrderByAppointmentDateDesc(String phone);

    List<AppointmentEntity> findByAppointmentDateOrderByTimeSlotAsc(LocalDate date);

    @Query("SELECT COUNT(a) FROM AppointmentEntity a " +
           "WHERE a.appointmentDate = :date " +
           "AND a.timeSlot = :slot " +
           "AND a.status <> :cancelledStatus")
    long countActiveAppointmentsBySlot(
            @Param("date") LocalDate date,
            @Param("slot") String slot,
            @Param("cancelledStatus") AppointmentStatus cancelledStatus
    );
}
