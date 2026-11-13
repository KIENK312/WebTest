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
     * Trang chủ chính thức kết hợp Spring Web MVC & Thymeleaf
     */
    @GetMapping({"/", "/index", "/mvc"})
    public String index(Model model) {
        TripSearchRequest defaultSearch = TripSearchRequest.builder()
                .departureDate(LocalDate.now().plusDays(1))
                .build();
        List<TripSearchResponse> trips = tripService.searchTrips(defaultSearch);
        model.addAttribute("trips", trips);
        model.addAttribute("searchRequest", defaultSearch);
        return "index";
    }

    /**
     * Tra cứu danh sách chuyến xe khách du lịch
     */
    @GetMapping({"/trips", "/mvc/trips"})
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
     * Xem sơ đồ 38 ghế 2 tầng & Biểu mẫu giữ chỗ (PRG Step 1)
     */
    @GetMapping({"/trips/{id}/booking", "/mvc/trips/{id}/booking", "/seat-selection"})
    public String bookingPage(
            @PathVariable(value = "id", required = false) Long tripId,
            @RequestParam(value = "tripId", required = false) Long paramTripId,
            Model model) {

        Long targetTripId = tripId != null ? tripId : paramTripId;
        if (targetTripId == null) {
            targetTripId = 1L;
        }

        TripSearchResponse trip = tripService.getTripById(targetTripId);
        List<TripSeatResponse> seats = tripService.getTripSeats(targetTripId);

        model.addAttribute("trip", trip);
        model.addAttribute("seats", seats);

        if (!model.containsAttribute("bookingRequest")) {
            model.addAttribute("bookingRequest", HoldSeatRequest.builder()
                    .tripId(targetTripId)
                    .build());
        }

        return "booking";
    }

    /**
     * Tra cứu vé xe điện tử & Tem số
     */
    @GetMapping({"/my-tickets", "/mvc/my-tickets"})
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
    @GetMapping({"/login", "/mvc/login"})
    public String loginPage() {
        return "login";
    }

    /**
     * Đăng xuất khỏi hệ thống: Xóa HttpOnly cookie jwt_token và chuyển về trang đăng nhập
     */
    @GetMapping("/logout")
    public String logout(jakarta.servlet.http.HttpServletResponse response) {
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("jwt_token", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/login";
    }
}
