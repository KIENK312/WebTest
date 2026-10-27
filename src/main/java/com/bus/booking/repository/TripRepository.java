package com.bus.booking.repository;

import com.bus.booking.entity.TripEntity;
import com.bus.booking.enums.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<TripEntity, Long> {

    /**
     * Tìm kiếm chuyến xe theo điểm xuất phát, điểm đến, khoảng thời gian khởi hành,
     * đồng thời chỉ lấy các chuyến có phương tiện xe đang hoạt động (bus.status = 'ACTIVE')
     * và trạng thái chuyến chưa bị huỷ (status <> CANCELLED).
     * Hỗ trợ tham số null / rỗng để lấy toàn bộ chuyến mở bán.
     */
    @Query("SELECT t FROM TripEntity t " +
           "JOIN FETCH t.route r " +
           "JOIN FETCH r.operator ro " +
           "JOIN FETCH t.bus b " +
           "JOIN FETCH b.operator o " +
           "WHERE (:departurePoint IS NULL OR :departurePoint = '' OR LOWER(r.origin) LIKE LOWER(CONCAT('%', :departurePoint, '%'))) " +
           "  AND (:arrivalPoint IS NULL OR :arrivalPoint = '' OR LOWER(r.destination) LIKE LOWER(CONCAT('%', :arrivalPoint, '%'))) " +
           "  AND (:startTime IS NULL OR t.departureTime >= :startTime) " +
           "  AND (:endTime IS NULL OR t.departureTime <= :endTime) " +
           "  AND b.status = com.bus.booking.enums.BusStatus.ACTIVE " +
           "  AND t.status <> com.bus.booking.enums.TripStatus.CANCELLED " +
           "ORDER BY t.departureTime ASC")
    List<TripEntity> findAvailableTrips(
            @Param("departurePoint") String departurePoint,
            @Param("arrivalPoint") String arrivalPoint,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * Lấy toàn bộ chuyến xe hợp lệ đang mở bán trong hệ thống.
     */
    @Query("SELECT t FROM TripEntity t " +
           "JOIN FETCH t.route r " +
           "JOIN FETCH r.operator ro " +
           "JOIN FETCH t.bus b " +
           "JOIN FETCH b.operator o " +
           "WHERE b.status = com.bus.booking.enums.BusStatus.ACTIVE " +
           "  AND t.status <> com.bus.booking.enums.TripStatus.CANCELLED " +
           "ORDER BY t.departureTime ASC")
    List<TripEntity> findAllAvailableTrips();

    List<TripEntity> findByRouteId(Long routeId);

    List<TripEntity> findByBusId(Long busId);

    List<TripEntity> findByStatus(TripStatus status);

    /**
     * Tìm kiếm các chuyến xe của 1 xe cụ thể trong khoảng thời gian (dùng cho kiểm tra đệm 48h bảo trì).
     */
    @Query("SELECT t FROM TripEntity t " +
           "WHERE t.bus.id = :busId " +
           "  AND t.status = :status " +
           "  AND t.departureTime <= :bufferEnd " +
           "  AND t.arrivalTime >= :bufferStart")
    List<TripEntity> findConflictingTrips(
            @Param("busId") Long busId,
            @Param("status") TripStatus status,
            @Param("bufferStart") LocalDateTime bufferStart,
            @Param("bufferEnd") LocalDateTime bufferEnd
    );

    @Query("SELECT t FROM TripEntity t " +
           "JOIN FETCH t.route r " +
           "JOIN FETCH r.operator ro " +
           "JOIN FETCH t.bus b " +
           "JOIN FETCH b.operator o " +
           "WHERE o.id = :operatorId " +
           "ORDER BY t.departureTime DESC")
    List<TripEntity> findByOperatorIdWithDetails(@Param("operatorId") Long operatorId);

    @Query("SELECT t FROM TripEntity t " +
           "JOIN FETCH t.route r " +
           "JOIN FETCH r.operator ro " +
           "JOIN FETCH t.bus b " +
           "JOIN FETCH b.operator o " +
           "ORDER BY t.departureTime DESC")
    List<TripEntity> findAllWithDetails();

    @Query("SELECT t FROM TripEntity t " +
           "JOIN FETCH t.route r " +
           "JOIN FETCH r.operator ro " +
           "JOIN FETCH t.bus b " +
           "JOIN FETCH b.operator o " +
           "WHERE t.id = :id")
    Optional<TripEntity> findByIdWithDetails(@Param("id") Long id);
}
