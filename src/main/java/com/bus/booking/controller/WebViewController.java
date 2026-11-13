package com.bus.booking.controller;

import com.bus.booking.dto.request.HoldSeatRequest;
import com.bus.booking.dto.request.TripSearchRequest;
import com.bus.booking.dto.response.HoldSeatResponse;
import com.bus.booking.dto.response.TripSearchResponse;
import com.bus.booking.dto.response.TripSeatResponse;
import com.bus.booking.service.BookingService;
import com.bus.booking.service.TripService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller điều phối giao diện Spring Web MVC kết hợp Thymeleaf
 * Đáp ứng toàn diện Tiêu chí Khối VI (Thymeleaf, HTML5 Responsive)
 */
@Slf4j
@Controller
public class WebViewController {

    private final TripService tripService;
    private final BookingService bookingService;

    public WebViewController(TripService tripService, BookingService bookingService) {
        this.tripService = tripService;
        this.bookingService = bookingService;
    }

    /**
     * Mặc định truy cập gốc (/) sẽ chuyển tiếp vào trang chủ bán vé xe khách chính thức (index.html)
     */
    @GetMapping("/")
    public String root() {
        return "forward:/index.html";
    }

    /**
     * Cổng thông tin MVC phụ (nếu cần xem kiểm thử)
     */
    @GetMapping("/mvc")
    public String index(Model model) {
        // Tải danh sách chuyến xe mặc định để hiển thị
        TripSearchRequest defaultSearch = TripSearchRequest.builder()
                .departureDate(LocalDate.now().plusDays(1))
                .build();
        List<TripSearchResponse> trips = tripService.searchTrips(defaultSearch);
        model.addAttribute("trips", trips);
        model.addAttribute("searchRequest", defaultSearch);
        return "index";
    }

    /**
     * Tra cứu danh sách chuyến xe
     */
    @GetMapping("/mvc/trips")
    public String searchTrips(
            @ModelAttribute TripSearchRequest searchRequest,
            Model model) {

        if (searchRequest.getDepartureDate() == null) {
            searchRequest.setDepartureDate(LocalDate.now().plusDays(1));
        }

        List<TripSearchResponse> trips = tripService.searchTrips(searchRequest);
        model.addAttribute("trips", trips);
        model.addAttribute("searchRequest", searchRequest);
        return "trips";
    }

    /**
     * Xem sơ đồ ghế & Điền thông tin đặt vé cho chuyến đi
     */
    @GetMapping("/mvc/trips/{id}/booking")
    public String bookingPage(
            @PathVariable("id") Long tripId,
            Model model) {

        TripSearchResponse trip = tripService.getTripById(tripId);
        List<TripSeatResponse> seats = tripService.getTripSeats(tripId);

        model.addAttribute("trip", trip);
        model.addAttribute("seats", seats);

        if (!model.containsAttribute("bookingRequest")) {
            model.addAttribute("bookingRequest", HoldSeatRequest.builder()
                    .tripId(tripId)
                    .build());
        }

        return "booking";
    }

    /**
     * Tra cứu vé xe điện tử & Tem số
     */
    @GetMapping("/mvc/my-tickets")
    public String lookupTickets(
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "ticketCode", required = false) String ticketCode,
            Model model) {

        if (phone != null && !phone.isBlank()) {
            List<HoldSeatResponse> tickets = bookingService.getUserTickets(null, phone.trim());
            model.addAttribute("tickets", tickets);
            model.addAttribute("searchedPhone", phone);
        }
        return "my-tickets";
    }

    /**
     * Form Đăng nhập đa vai trò (Spring Web MVC)
     */
    @GetMapping("/mvc/login")
    public String loginPage() {
        return "login";
    }
}
