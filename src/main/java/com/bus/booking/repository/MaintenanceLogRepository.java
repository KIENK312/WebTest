package com.bus.booking.repository;

import com.bus.booking.entity.MaintenanceLogEntity;
import com.bus.booking.enums.MaintenanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MaintenanceLogRepository extends JpaRepository<MaintenanceLogEntity, Long> {

    /**
     * Kiểm tra xung đột lịch bảo trì theo chuẩn Spring Data JPA:
     * Trả về true nếu xe đã có lịch bảo trì bắt đầu trước mốc endTime và kết thúc sau mốc startTime.
     */
    boolean existsByBusIdAndStartTimeBeforeAndEndTimeAfter(Long busId, LocalDateTime endTime, LocalDateTime startTime);

    /**
     * Truy vấn toàn diện bao gồm cả các bản ghi bảo trì đang diễn ra (endTime có thể là NULL).
     */
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN TRUE ELSE FALSE END " +
           "FROM MaintenanceLogEntity m " +
           "WHERE m.bus.id = :busId " +
           "  AND m.status = com.bus.booking.enums.MaintenanceStatus.IN_PROGRESS " +
           "  AND (m.startTime <= :endTime AND (m.endTime IS NULL OR m.endTime >= :startTime))")
    boolean hasOverlappingMaintenance(
            @Param("busId") Long busId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * Tối ưu hóa N+1: Kiểm tra đồng loạt danh sách busId có lịch bảo trì trùng trong khoảng thời gian.
     */
    @Query("SELECT DISTINCT m.bus.id FROM MaintenanceLogEntity m " +
           "WHERE m.bus.id IN :busIds " +
           "  AND m.status = com.bus.booking.enums.MaintenanceStatus.IN_PROGRESS " +
           "  AND (m.startTime <= :endTime AND (m.endTime IS NULL OR m.endTime >= :startTime))")
    List<Long> findBusIdsInMaintenance(
            @Param("busIds") List<Long> busIds,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    List<MaintenanceLogEntity> findByBusId(Long busId);

    List<MaintenanceLogEntity> findByBusIdAndStatus(Long busId, MaintenanceStatus status);

    @Query("SELECT m FROM MaintenanceLogEntity m " +
           "JOIN FETCH m.bus b " +
           "JOIN FETCH b.operator o " +
           "WHERE b.id = :busId " +
           "ORDER BY m.startTime DESC")
    List<MaintenanceLogEntity> findByBusIdWithDetails(@Param("busId") Long busId);

    @Query("SELECT m FROM MaintenanceLogEntity m " +
           "JOIN FETCH m.bus b " +
           "JOIN FETCH b.operator o " +
           "WHERE o.id = :operatorId " +
           "ORDER BY m.startTime DESC")
    List<MaintenanceLogEntity> findByOperatorIdWithDetails(@Param("operatorId") Long operatorId);
}
