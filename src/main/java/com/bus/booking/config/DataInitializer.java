package com.bus.booking.config;

import com.bus.booking.entity.*;
import com.bus.booking.enums.*;
import com.bus.booking.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final BusOperatorRepository busOperatorRepository;
    private final UserRepository userRepository;
    private final RouteRepository routeRepository;
    private final BusRepository busRepository;
    private final SeatRepository seatRepository;
    private final TripRepository tripRepository;
    private final TicketRepository ticketRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("--- Khởi chạy DataInitializer: Đồng bộ dữ liệu nền tảng hệ thống ---");

        // 1. Roles
        RoleEntity roleAdmin = roleRepository.findByName("ROLE_ADMIN").orElseGet(() ->
                roleRepository.save(RoleEntity.builder().name("ROLE_ADMIN").description("Quản trị viên toàn hệ thống").build())
        );
        RoleEntity roleOperator = roleRepository.findByName("ROLE_OPERATOR").orElseGet(() ->
                roleRepository.save(RoleEntity.builder().name("ROLE_OPERATOR").description("Nhân viên quản lý nhà xe").build())
        );
        RoleEntity roleCustomer = roleRepository.findByName("ROLE_CUSTOMER").orElseGet(() ->
                roleRepository.save(RoleEntity.builder().name("ROLE_CUSTOMER").description("Khách hàng đặt vé").build())
        );

        // 2. Bus Operators
        BusOperatorEntity futa = busOperatorRepository.findByCode("FUTA").orElseGet(() ->
                busOperatorRepository.save(BusOperatorEntity.builder()
                        .name("Công ty Cổ phần Xe khách Phương Trang (FUTA Bus Lines)")
                        .code("FUTA")
                        .contactPhone("19006067")
                        .contactEmail("hotro@futa.vn")
                        .address("01 Tô Hiến Thành, Phường 3, TP. Đà Lạt, Lâm Đồng")
                        .status(OperatorStatus.ACTIVE)
                        .build())
        );

        BusOperatorEntity hoangLong = busOperatorRepository.findByCode("HOANGLONG").orElseGet(() ->
                busOperatorRepository.save(BusOperatorEntity.builder()
                        .name("Công ty TNHH Vận tải Hoàng Long (Hoàng Long Asia)")
                        .code("HOANGLONG")
                        .contactPhone("02253920920")
                        .contactEmail("info@hoanglongasia.com")
                        .address("Số 05 Phạm Ngũ Lão, Lương Khánh Thiện, Ngô Quyền, Hải Phòng")
                        .status(OperatorStatus.ACTIVE)
                        .build())
        );

        // 3. Users (Tạo sẵn 4 tài khoản chuẩn)
        if (!userRepository.existsByUsername("admin")) {
            userRepository.save(UserEntity.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("Admin@123"))
                    .fullName("Quản Trị Viên Hệ Thống")
                    .email("admin@busbooking.vn")
                    .phoneNumber("0901112233")
                    .role(roleAdmin)
                    .status(UserStatus.ACTIVE)
                    .build());
            log.info("Đã tạo tài khoản ADMIN: admin / Admin@123");
        }

        if (!userRepository.existsByUsername("operator_futa")) {
            userRepository.save(UserEntity.builder()
                    .username("operator_futa")
                    .password(passwordEncoder.encode("Operator@123"))
                    .fullName("Điều Hành Xe Phương Trang")
                    .email("operator@futa.vn")
                    .phoneNumber("0902223344")
                    .role(roleOperator)
                    .operator(futa)
                    .status(UserStatus.ACTIVE)
                    .build());
            log.info("Đã tạo tài khoản OPERATOR: operator_futa / Operator@123");
        }

        if (!userRepository.existsByUsername("operator_hoanglong")) {
            userRepository.save(UserEntity.builder()
                    .username("operator_hoanglong")
                    .password(passwordEncoder.encode("Operator@123"))
                    .fullName("Điều Hành Xe Hoàng Long")
                    .email("operator@hoanglong.vn")
                    .phoneNumber("0903334455")
                    .role(roleOperator)
                    .operator(hoangLong)
                    .status(UserStatus.ACTIVE)
                    .build());
            log.info("Đã tạo tài khoản OPERATOR: operator_hoanglong / Operator@123");
        }

        if (!userRepository.existsByUsername("customer")) {
            userRepository.save(UserEntity.builder()
                    .username("customer")
                    .password(passwordEncoder.encode("Customer@123"))
                    .fullName("Nguyễn Văn Khách")
                    .email("customer@gmail.com")
                    .phoneNumber("0987654321")
                    .role(roleCustomer)
                    .status(UserStatus.ACTIVE)
                    .build());
            log.info("Đã tạo tài khoản CUSTOMER: customer / Customer@123");
        }

        // 4. Tuyến đường (Routes)
        if (routeRepository.count() == 0) {
            routeRepository.save(RouteEntity.builder()
                    .operator(futa).origin("Hà Nội").destination("Đà Nẵng")
                    .distanceKm(new BigDecimal("770.0")).estimatedDurationHours(new BigDecimal("14.0"))
                    .status("ACTIVE").build());

            routeRepository.save(RouteEntity.builder()
                    .operator(futa).origin("Sài Gòn").destination("Đà Lạt")
                    .distanceKm(new BigDecimal("310.0")).estimatedDurationHours(new BigDecimal("8.0"))
                    .status("ACTIVE").build());

            routeRepository.save(RouteEntity.builder()
                    .operator(hoangLong).origin("Hà Nội").destination("Sa Pa")
                    .distanceKm(new BigDecimal("320.0")).estimatedDurationHours(new BigDecimal("6.0"))
                    .status("ACTIVE").build());

            routeRepository.save(RouteEntity.builder()
                    .operator(hoangLong).origin("Sài Gòn").destination("Nha Trang")
                    .distanceKm(new BigDecimal("430.0")).estimatedDurationHours(new BigDecimal("9.0"))
                    .status("ACTIVE").build());

            routeRepository.save(RouteEntity.builder()
                    .operator(futa).origin("Sài Gòn").destination("Cần Thơ")
                    .distanceKm(new BigDecimal("170.0")).estimatedDurationHours(new BigDecimal("3.5"))
                    .status("ACTIVE").build());

            log.info("Đã khởi tạo 5 tuyến đường phổ biến");
        }

        // 5. Đội xe (Buses)
        if (busRepository.count() == 0) {
            BusEntity bus1 = busRepository.save(BusEntity.builder()
                    .operator(futa).plateNumber("51B-123.45").busType("Giường nằm 36 chỗ")
                    .totalSeats(36).status(BusStatus.ACTIVE).build());

            BusEntity bus2 = busRepository.save(BusEntity.builder()
                    .operator(futa).plateNumber("51B-987.65").busType("Limousine VIP 24 phòng")
                    .totalSeats(24).status(BusStatus.ACTIVE).build());

            BusEntity bus3 = busRepository.save(BusEntity.builder()
                    .operator(futa).plateNumber("51B-555.55").busType("Giường nằm 36 chỗ")
                    .totalSeats(36).status(BusStatus.MAINTENANCE).build()); // Xe bảo dưỡng để test cảnh báo

            BusEntity bus4 = busRepository.save(BusEntity.builder()
                    .operator(hoangLong).plateNumber("29B-456.78").busType("Giường nằm 36 chỗ")
                    .totalSeats(36).status(BusStatus.ACTIVE).build());

            BusEntity bus5 = busRepository.save(BusEntity.builder()
                    .operator(hoangLong).plateNumber("29B-888.88").busType("Limousine VIP 24 phòng")
                    .totalSeats(24).status(BusStatus.ACTIVE).build());

            // 6. Ghế cho từng xe (Seats 2 tầng: Tầng 1 A01..A18, Tầng 2 B01..B18)
            createSeatsForBus(bus1, 36, "STANDARD");
            createSeatsForBus(bus2, 24, "VIP");
            createSeatsForBus(bus3, 36, "STANDARD");
            createSeatsForBus(bus4, 36, "STANDARD");
            createSeatsForBus(bus5, 24, "VIP");

            log.info("Đã khởi tạo đội xe và sơ đồ ghế 2 tầng");
        }

        // 7. Chuyến xe (Trips)
        if (tripRepository.count() == 0) {
            List<RouteEntity> allRoutes = routeRepository.findAll();
            List<BusEntity> activeBuses = busRepository.findByStatus(BusStatus.ACTIVE);

            LocalDate today = LocalDate.now();
            List<TripEntity> tripsToSave = new ArrayList<>();

            for (int dayOffset = 0; dayOffset <= 5; dayOffset++) {
                LocalDate tripDate = today.plusDays(dayOffset);

                for (RouteEntity route : allRoutes) {
                    BusEntity assignedBus = activeBuses.stream()
                            .filter(b -> b.getOperator().getId().equals(route.getOperator().getId()))
                            .findFirst()
                            .orElse(activeBuses.get(0));

                    BigDecimal price = route.getDestination().contains("Đà Nẵng") ? new BigDecimal("450000.00")
                            : route.getDestination().contains("Đà Lạt") ? new BigDecimal("320000.00")
                            : route.getDestination().contains("Sa Pa") ? new BigDecimal("280000.00")
                            : route.getDestination().contains("Nha Trang") ? new BigDecimal("350000.00")
                            : new BigDecimal("190000.00");

                    // Chuyến sáng
                    tripsToSave.add(TripEntity.builder()
                            .route(route)
                            .bus(assignedBus)
                            .departureTime(tripDate.atTime(7, 30))
                            .arrivalTime(tripDate.atTime(7, 30).plusHours(route.getEstimatedDurationHours().longValue()))
                            .basePrice(price)
                            .status(TripStatus.SCHEDULED)
                            .build());

                    // Chuyến chiều / tối
                    tripsToSave.add(TripEntity.builder()
                            .route(route)
                            .bus(assignedBus)
                            .departureTime(tripDate.atTime(19, 0))
                            .arrivalTime(tripDate.atTime(19, 0).plusHours(route.getEstimatedDurationHours().longValue()))
                            .basePrice(price.add(new BigDecimal("30000.00")))
                            .status(TripStatus.SCHEDULED)
                            .build());
                }
            }

            tripRepository.saveAll(tripsToSave);
            log.info("Đã khởi tạo {} chuyến xe lịch trình phong phú", tripsToSave.size());
        }

        // 8. Vé mẫu (Tickets) phục vụ thống kê thực tế cho Admin Dashboard
        if (ticketRepository.count() == 0 && tripRepository.count() > 0) {
            List<TripEntity> trips = tripRepository.findAll();
            TripEntity trip1 = trips.get(0);
            TripEntity trip2 = trips.size() > 1 ? trips.get(1) : trip1;
            UserEntity customerUser = userRepository.findByUsername("customer").orElse(null);
            List<SeatEntity> trip1Seats = seatRepository.findByBusIdOrderBySeatNumberAsc(trip1.getBus().getId());
            List<SeatEntity> trip2Seats = seatRepository.findByBusIdOrderBySeatNumberAsc(trip2.getBus().getId());

            if (!trip1Seats.isEmpty()) {
                ticketRepository.save(TicketEntity.builder()
                        .trip(trip1)
                        .seat(trip1Seats.get(0))
                        .user(customerUser)
                        .customerName("Nguyễn Văn Khách")
                        .customerPhone("0987654321")
                        .customerEmail("customer@gmail.com")
                        .ticketCode("TK-SAMPLE-01")
                        .price(trip1.getBasePrice())
                        .status(TicketStatus.CONFIRMED)
                        .bookingTime(LocalDateTime.now().minusHours(2))
                        .build());
            }

            if (trip1Seats.size() > 1) {
                ticketRepository.save(TicketEntity.builder()
                        .trip(trip1)
                        .seat(trip1Seats.get(1))
                        .user(customerUser)
                        .customerName("Trần Minh Tú")
                        .customerPhone("0912345678")
                        .customerEmail("tu@gmail.com")
                        .ticketCode("TK-SAMPLE-02")
                        .price(trip1.getBasePrice())
                        .status(TicketStatus.CONFIRMED)
                        .bookingTime(LocalDateTime.now().minusDays(1))
                        .build());
            }

            if (!trip2Seats.isEmpty()) {
                ticketRepository.save(TicketEntity.builder()
                        .trip(trip2)
                        .seat(trip2Seats.get(0))
                        .customerName("Khách Vãng Lai")
                        .customerPhone("0909998877")
                        .customerEmail("vanglai@gmail.com")
                        .ticketCode("TK-SAMPLE-03")
                        .price(trip2.getBasePrice())
                        .status(TicketStatus.CANCELLED)
                        .bookingTime(LocalDateTime.now().minusDays(2))
                        .build());
            }
            log.info("Đã khởi tạo vé mẫu cho hệ thống");
        }

        log.info("--- DataInitializer hoàn tất thành công! ---");
    }

    private void createSeatsForBus(BusEntity bus, int totalSeats, String seatType) {
        int seatsPerFloor = totalSeats / 2;
        List<SeatEntity> seats = new ArrayList<>();

        // Tầng 1: A01 -> A18
        for (int i = 1; i <= seatsPerFloor; i++) {
            seats.add(SeatEntity.builder()
                    .bus(bus)
                    .floorNumber(1)
                    .seatNumber(String.format("A%02d", i))
                    .seatType(seatType)
                    .build());
        }

        // Tầng 2: B01 -> B18
        for (int i = 1; i <= seatsPerFloor; i++) {
            seats.add(SeatEntity.builder()
                    .bus(bus)
                    .floorNumber(2)
                    .seatNumber(String.format("B%02d", i))
                    .seatType(seatType)
                    .build());
        }

        seatRepository.saveAll(seats);
    }
}
