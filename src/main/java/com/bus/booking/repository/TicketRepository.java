package com.bus.booking.repository;

import com.bus.booking.entity.TicketEntity;
import com.bus.booking.enums.TicketStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<TicketEntity, Long> {

    /**
     * Kiểm tra sự tồn tại của vé theo mã chuyến, mã ghế và danh sách trạng thái
     * (thường dùng để xác định ghế đã được giữ chỗ hoặc đã bán hay chưa).
     */
    boolean existsByTripIdAndSeatIdAndStatusIn(Long tripId, Long seatId, List<TicketStatus> statuses);

    /**
     * Kiểm tra ghế hợp lệ theo cutoff 10 phút (chống giữ chỗ quá hạn).
     */
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN TRUE ELSE FALSE END FROM TicketEntity t " +
           "WHERE t.trip.id = :tripId AND t.seat.id = :seatId " +
           "  AND (t.status = com.bus.booking.enums.TicketStatus.CONFIRMED " +
           "       OR (t.status = com.bus.booking.enums.TicketStatus.PENDING AND t.bookingTime >= :cutoffTime))")
    boolean existsValidActiveTicket(
            @Param("tripId") Long tripId,
            @Param("seatId") Long seatId,
            @Param("cutoffTime") LocalDateTime cutoffTime
    );

    /**
     * Lấy danh sách vé còn hiệu lực (CONFIRMED hoặc PENDING chưa quá 10 phút).
     */
    @Query("SELECT t FROM TicketEntity t " +
           "JOIN FETCH t.seat s " +
           "WHERE t.trip.id = :tripId " +
           "  AND (t.status = com.bus.booking.enums.TicketStatus.CONFIRMED " +
           "       OR (t.status = com.bus.booking.enums.TicketStatus.PENDING AND t.bookingTime >= :cutoffTime))")
    List<TicketEntity> findValidActiveTicketsByTripId(
            @Param("tripId") Long tripId,
            @Param("cutoffTime") LocalDateTime cutoffTime
    );

    /**
     * Tối ưu hóa N+1: Đếm số vé đã đặt cho danh sách chuyến xe trong 1 câu SQL duy nhất.
     */
    @Query("SELECT t.trip.id, COUNT(t) FROM TicketEntity t " +
           "WHERE t.trip.id IN :tripIds " +
           "  AND (t.status = com.bus.booking.enums.TicketStatus.CONFIRMED " +
           "       OR (t.status = com.bus.booking.enums.TicketStatus.PENDING AND t.bookingTime >= :cutoffTime)) " +
           "GROUP BY t.trip.id")
    List<Object[]> countBookedSeatsByTripIds(
            @Param("tripIds") List<Long> tripIds,
            @Param("cutoffTime") LocalDateTime cutoffTime
    );

    /**
     * Scheduled Worker: Tự động hết hạn các vé PENDING giữ chỗ quá 10 phút.
     */
    @Modifying
    @Query("UPDATE TicketEntity t SET t.status = com.bus.booking.enums.TicketStatus.EXPIRED " +
           "WHERE t.status = com.bus.booking.enums.TicketStatus.PENDING " +
           "  AND t.bookingTime < :cutoffTime")
    int expireOldPendingTickets(@Param("cutoffTime") LocalDateTime cutoffTime);

    Optional<TicketEntity> findByTicketCode(String ticketCode);

    List<TicketEntity> findByTripId(Long tripId);

    List<TicketEntity> findByUserId(Long userId);

    List<TicketEntity> findByTripIdAndStatus(Long tripId, TicketStatus status);

    List<TicketEntity> findByTripIdAndStatusIn(Long tripId, List<TicketStatus> statuses);

    long countByTripIdAndStatusIn(Long tripId, List<TicketStatus> statuses);

    /**
     * Tính tổng doanh thu thực tế từ vé đã xác nhận (CONFIRMED).
     * Dùng COALESCE để không bao giờ trả về null.
     */
    @Query("SELECT COALESCE(SUM(t.price), 0) FROM TicketEntity t WHERE t.status = com.bus.booking.enums.TicketStatus.CONFIRMED")
    BigDecimal calculateTotalRevenue();

    /**
     * Đếm tổng số vé đã bán thành công.
     */
    @Query("SELECT COUNT(t) FROM TicketEntity t WHERE t.status = com.bus.booking.enums.TicketStatus.CONFIRMED")
    long countConfirmedTickets();

    /**
     * Đếm số vé theo trạng thái.
     */
    long countByStatus(TicketStatus status);

    /**
     * Lấy danh sách vé gần đây nhất kèm đầy đủ quan hệ (JOIN FETCH).
     */
    @Query("SELECT t FROM TicketEntity t " +
           "JOIN FETCH t.trip tr " +
           "JOIN FETCH tr.route r " +
           "JOIN FETCH tr.bus b " +
           "JOIN FETCH t.seat s " +
           "ORDER BY t.bookingTime DESC")
    List<TicketEntity> findRecentTicketsWithDetails(Pageable pageable);

    /**
     * Lấy danh sách vé đã xác nhận trong khoảng thời gian để tính doanh thu theo tháng.
     */
    @Query("SELECT t FROM TicketEntity t " +
           "WHERE t.status = com.bus.booking.enums.TicketStatus.CONFIRMED " +
           "  AND t.bookingTime >= :since " +
           "ORDER BY t.bookingTime ASC")
    List<TicketEntity> findConfirmedTicketsSince(@Param("since") LocalDateTime since);

    /**
     * Lấy lịch sử vé của người dùng kèm đầy đủ quan hệ.
     */
    @Query("SELECT t FROM TicketEntity t " +
           "JOIN FETCH t.trip tr " +
           "JOIN FETCH tr.route r " +
           "JOIN FETCH tr.bus b " +
           "JOIN FETCH t.seat s " +
           "WHERE t.user.id = :userId " +
           "ORDER BY t.bookingTime DESC")
    List<TicketEntity> findByUserIdWithDetails(@Param("userId") Long userId);
}
