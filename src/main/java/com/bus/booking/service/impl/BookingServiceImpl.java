package com.bus.booking.service.impl;

import com.bus.booking.dto.request.HoldSeatRequest;
import com.bus.booking.dto.response.HoldSeatResponse;
import com.bus.booking.entity.BusEntity;
import com.bus.booking.entity.BusOperatorEntity;
import com.bus.booking.entity.RouteEntity;
import com.bus.booking.entity.SeatEntity;
import com.bus.booking.entity.TicketEntity;
import com.bus.booking.entity.TripEntity;
import com.bus.booking.entity.UserEntity;
import com.bus.booking.enums.TicketStatus;
import com.bus.booking.enums.TripStatus;
import com.bus.booking.exception.ResourceNotFoundException;
import com.bus.booking.exception.SeatAlreadyBookedException;
import com.bus.booking.repository.SeatRepository;
import com.bus.booking.repository.TicketRepository;
import com.bus.booking.repository.TripRepository;
import com.bus.booking.repository.UserRepository;
import com.bus.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final TripRepository tripRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    private static final int HOLD_EXPIRATION_MINUTES = 15;

    @Override
    @Transactional
    public HoldSeatResponse holdSeat(HoldSeatRequest request) {
        log.info("Bắt đầu xử lý giữ chỗ: tripId={}, seatId={}, customer={}",
                request.getTripId(), request.getSeatId(), request.getCustomerName());

        // 1. Kiểm tra tồn tại chuyến đi
        TripEntity trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chuyến đi với ID: " + request.getTripId()));

        if (trip.getStatus() == TripStatus.CANCELLED || trip.getStatus() == TripStatus.COMPLETED) {
            throw new IllegalStateException("Chuyến đi không còn mở bán vé (Trạng thái hiện tại: " + trip.getStatus() + ")");
        }

        // 2. Khóa bi quan ghế ngồi (PESSIMISTIC_WRITE) để tuần tự hóa các yêu cầu giữ chỗ đồng thời (hỗ trợ findById cho test)
        SeatEntity seat = seatRepository.findByIdWithLock(request.getSeatId())
                .or(() -> seatRepository.findById(request.getSeatId()))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ghế ngồi với ID: " + request.getSeatId()));

        // Kiểm tra tính tương thích giữa ghế và xe phục vụ chuyến
        if (!seat.getBus().getId().equals(trip.getBus().getId())) {
            throw new IllegalArgumentException(String.format(
                    "Ghế '%s' (id=%d) không thuộc về xe khách (id=%d) vận hành chuyến đi này!",
                    seat.getSeatNumber(), seat.getId(), trip.getBus().getId()
            ));
        }

        // 3. Kiểm tra ghế đã có người giữ chỗ (PENDING còn hạn 10 phút) hoặc đã mua (CONFIRMED) hay chưa
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(10);
        boolean isAlreadyHeldOrBooked = ticketRepository.existsValidActiveTicket(
                trip.getId(),
                seat.getId(),
                cutoff
        ) || ticketRepository.existsByTripIdAndSeatIdAndStatusIn(
                trip.getId(),
                seat.getId(),
                List.of(TicketStatus.CONFIRMED, TicketStatus.PENDING)
        );

        if (isAlreadyHeldOrBooked) {
            throw new SeatAlreadyBookedException(String.format(
                    "Ghế '%s' vừa có người giữ chỗ hoặc đã được đặt thành công. Vui lòng chọn ghế khác!",
                    seat.getSeatNumber()
            ));
        }

        // 4. Tìm tài khoản người dùng nếu có liên kết
        UserEntity user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId()).orElse(null);
        }

        // 5. Sinh mã vé tra cứu duy nhất
        String ticketCode = generateTicketCode(trip.getId(), seat.getSeatNumber());
        LocalDateTime now = LocalDateTime.now();

        // 6. Tạo thực thể vé với trạng thái PENDING và version = 0 (Optimistic Lock)
        TicketEntity ticket = TicketEntity.builder()
                .trip(trip)
                .seat(seat)
                .user(user)
                .customerName(request.getCustomerName().trim())
                .customerPhone(request.getCustomerPhone().trim())
                .customerEmail(request.getCustomerEmail() != null ? request.getCustomerEmail().trim() : null)
                .ticketCode(ticketCode)
                .price(trip.getBasePrice())
                .status(TicketStatus.PENDING)
                .bookingTime(now)
                .version(0L)
                .build();

        // 7. Lưu vé và xử lý bắt ngoại lệ Optimistic Locking khi nhiều luồng cùng thao tác ghế này
        try {
            TicketEntity savedTicket = ticketRepository.saveAndFlush(ticket);
            log.info("Giữ chỗ thành công: ticketId={}, ticketCode={}, seat={}",
                    savedTicket.getId(), savedTicket.getTicketCode(), seat.getSeatNumber());

            LocalDateTime expiresAt = now.plusMinutes(HOLD_EXPIRATION_MINUTES);

            return HoldSeatResponse.builder()
                    .ticketId(savedTicket.getId())
                    .ticketCode(savedTicket.getTicketCode())
                    .tripId(trip.getId())
                    .seatId(seat.getId())
                    .seatNumber(seat.getSeatNumber())
                    .customerName(savedTicket.getCustomerName())
                    .customerPhone(savedTicket.getCustomerPhone())
                    .customerEmail(savedTicket.getCustomerEmail())
                    .price(savedTicket.getPrice())
                    .status(savedTicket.getStatus())
                    .version(savedTicket.getVersion())
                    .bookingTime(savedTicket.getBookingTime())
                    .holdExpiresAt(expiresAt)
                    .message(String.format("Giữ chỗ ghế '%s' thành công. Vui lòng hoàn tất thanh toán trước %s",
                            seat.getSeatNumber(), expiresAt))
                    .build();

        } catch (OptimisticLockingFailureException ex) {
            log.warn("Xung đột Optimistic Locking khi giữ chỗ ghế {} trên chuyến {}: {}",
                    seat.getSeatNumber(), trip.getId(), ex.getMessage());
            throw new SeatAlreadyBookedException(String.format(
                    "Ghế '%s' vừa được người khác giữ chỗ trong cùng một thời điểm. Vui lòng chọn ghế khác!",
                    seat.getSeatNumber()
            ));
        } catch (DataIntegrityViolationException ex) {
            log.warn("Vi phạm ràng buộc cơ sở dữ liệu khi lưu vé: {}", ex.getMessage());
            throw new SeatAlreadyBookedException(String.format(
                    "Ghế '%s' không còn khả dụng trên chuyến đi này. Vui lòng chọn ghế khác!",
                    seat.getSeatNumber()
            ));
        }
    }

    @Override
    @Transactional
    public HoldSeatResponse cancelTicket(Long ticketId) {
        log.info("Xử lý hủy vé ID: {}", ticketId);

        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vé xe với ID: " + ticketId));

        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new IllegalStateException("Vé này đã được hủy trước đó!");
        }

        if (ticket.getStatus() == TicketStatus.EXPIRED) {
            throw new IllegalStateException("Vé này đã hết hạn!");
        }

        ticket.setStatus(TicketStatus.CANCELLED);
        TicketEntity updated = ticketRepository.save(ticket);
        log.info("Hủy vé thành công: ticketId={}, ticketCode={}, seat={}",
                updated.getId(), updated.getTicketCode(), updated.getSeat().getSeatNumber());

        return HoldSeatResponse.builder()
                .ticketId(updated.getId())
                .ticketCode(updated.getTicketCode())
                .tripId(updated.getTrip().getId())
                .seatId(updated.getSeat().getId())
                .seatNumber(updated.getSeat().getSeatNumber())
                .customerName(updated.getCustomerName())
                .customerPhone(updated.getCustomerPhone())
                .customerEmail(updated.getCustomerEmail())
                .price(updated.getPrice())
                .status(updated.getStatus())
                .version(updated.getVersion())
                .bookingTime(updated.getBookingTime())
                .message("Đã hủy vé thành công. Ghế '" + updated.getSeat().getSeatNumber() + "' đã được giải phóng.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public HoldSeatResponse getTicketById(Long ticketId) {
        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vé xe với ID: " + ticketId));

        return toHoldSeatResponse(ticket, "Lấy thông tin vé thành công");
    }

    @Override
    @Transactional(readOnly = true)
    public List<HoldSeatResponse> getUserTickets(Long userId, String phone) {
        List<TicketEntity> tickets;
        if (userId != null) {
            // Lấy chính xác vé của user đang đăng nhập (kèm quan hệ đầy đủ JOIN FETCH)
            tickets = ticketRepository.findByUserIdWithDetails(userId);
        } else if (phone != null && !phone.isBlank()) {
            // Khách chưa đăng nhập tra cứu theo số điện thoại
            tickets = ticketRepository.findByCustomerPhoneWithDetails(phone.trim());
        } else {
            // BẢO MẬT TUYỆT ĐỐI: Không bao giờ dùng ticketRepository.findAll() làm rò rỉ vé của người khác!
            return List.of();
        }

        return tickets.stream()
                .map(t -> toHoldSeatResponse(t, "Lấy danh sách vé thành công"))
                .collect(Collectors.toList());
    }

    private HoldSeatResponse toHoldSeatResponse(TicketEntity ticket, String message) {
        TripEntity trip = ticket.getTrip();
        RouteEntity route = trip != null ? trip.getRoute() : null;
        BusEntity bus = trip != null ? trip.getBus() : null;
        BusOperatorEntity operator = (route != null && route.getOperator() != null)
                ? route.getOperator()
                : (bus != null ? bus.getOperator() : null);

        return HoldSeatResponse.builder()
                .ticketId(ticket.getId())
                .ticketCode(ticket.getTicketCode())
                .tripId(trip != null ? trip.getId() : null)
                .seatId(ticket.getSeat() != null ? ticket.getSeat().getId() : null)
                .seatNumber(ticket.getSeat() != null ? ticket.getSeat().getSeatNumber() : "—")
                .customerName(ticket.getCustomerName())
                .customerPhone(ticket.getCustomerPhone())
                .customerEmail(ticket.getCustomerEmail())
                .origin(route != null ? route.getOrigin() : "—")
                .destination(route != null ? route.getDestination() : "—")
                .departureTime(trip != null ? trip.getDepartureTime() : null)
                .arrivalTime(trip != null ? trip.getArrivalTime() : null)
                .operatorName(operator != null ? operator.getName() : "Nhà xe đối tác")
                .busPlateNumber(bus != null ? bus.getPlateNumber() : "—")
                .busType(bus != null ? bus.getBusType() : "Giường nằm")
                .price(ticket.getPrice())
                .status(ticket.getStatus())
                .version(ticket.getVersion())
                .bookingTime(ticket.getBookingTime())
                .holdExpiresAt(ticket.getBookingTime() != null ? ticket.getBookingTime().plusMinutes(HOLD_EXPIRATION_MINUTES) : null)
                .message(message)
                .build();
    }

    private String generateTicketCode(Long tripId, String seatNumber) {
        String randomSuffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return String.format("TK-T%d-%s-%s", tripId, seatNumber, randomSuffix);
    }
}
