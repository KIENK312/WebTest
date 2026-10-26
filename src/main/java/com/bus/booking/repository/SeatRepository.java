package com.bus.booking.repository;

import com.bus.booking.entity.SeatEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<SeatEntity, Long> {
    List<SeatEntity> findByBusIdOrderBySeatNumberAsc(Long busId);
    Optional<SeatEntity> findByBusIdAndSeatNumber(Long busId, String seatNumber);
    boolean existsByBusIdAndSeatNumber(Long busId, String seatNumber);
    long countByBusId(Long busId);

    /**
     * Khóa bi quan (PESSIMISTIC_WRITE) ngăn chặn tuyệt đối Race Condition khi 2 khách cùng bấm giữ 1 ghế.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SeatEntity s WHERE s.id = :id")
    Optional<SeatEntity> findByIdWithLock(@Param("id") Long id);
}
