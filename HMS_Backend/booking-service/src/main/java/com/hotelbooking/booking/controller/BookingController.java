package com.hotelbooking.booking.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hotelbooking.booking.dto.request.CancelBookingRequest;
import com.hotelbooking.booking.dto.request.CheckInRequest;
import com.hotelbooking.booking.dto.request.CheckOutRequest;
import com.hotelbooking.booking.dto.request.CreateBookingRequest;
import com.hotelbooking.booking.dto.request.WalkInBookingRequest;
import com.hotelbooking.booking.dto.response.AvailabilityResponse;
import com.hotelbooking.booking.dto.response.BookingResponse;
import com.hotelbooking.booking.exception.AccessDeniedException;
import com.hotelbooking.booking.exception.ValidationException;
import com.hotelbooking.booking.service.BookingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /**
     * Check room availability for a hotel and date range.
     * Public endpoint - no authentication required.
     */
    @GetMapping("/check-availability")
    public ResponseEntity<?> checkAvailability(
            @RequestParam Long hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut
    ) {
        AvailabilityResponse response = bookingService.checkAvailability(hotelId, checkIn, checkOut);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Availability check completed successfully",
                "data", response
        ));
    }

    /**
     * Create a new booking.
     * Protected endpoint - requires authentication.
     */
    @PostMapping
    public ResponseEntity<?> createBooking(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Username", required = false) String username,
            @Valid @RequestBody CreateBookingRequest request
    ) {
        // Role check: GUEST, MANAGER, ADMIN allowed
        if (!"GUEST".equalsIgnoreCase(role) && !"MANAGER".equalsIgnoreCase(role) 
                && !"ADMIN".equalsIgnoreCase(role)) {
            throw new AccessDeniedException("Only GUEST, MANAGER, or ADMIN can create bookings");
        }

        // For now, use username as guest name if available
        String guestName = username != null ? username : "Guest";
        String guestEmail = userEmail != null ? userEmail : "";

        BookingResponse booking = bookingService.createBooking(
                request, userId, guestName, guestEmail, null, role);

        return ResponseEntity.status(201).body(Map.of(
                "success", true,
                "message", "Booking created successfully",
                "data", booking
        ));
    }

    /**
     * Create walk-in booking (receptionist only).
     */
    @PostMapping("/walk-in")
    public ResponseEntity<?> createWalkInBooking(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody WalkInBookingRequest request
    ) {
        // Only RECEPTIONIST can create walk-in bookings
        if (!"RECEPTIONIST".equalsIgnoreCase(role)) {
            throw new AccessDeniedException("Only RECEPTIONIST can create walk-in bookings");
        }

        BookingResponse booking = bookingService.createWalkInBooking(request, userId, role);

        return ResponseEntity.status(201).body(Map.of(
                "success", true,
                "message", "Walk-in booking created successfully",
                "data", booking
        ));
    }

    /**
     * Get booking by ID.
     * Protected endpoint - user can only view their own bookings unless ADMIN/MANAGER.
     */
    @GetMapping("/{bookingId}")
    public ResponseEntity<?> getBookingById(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long bookingId
    ) {
        BookingResponse booking = bookingService.getBookingById(bookingId);

        // Authorization: Users can only view their own bookings unless ADMIN/MANAGER
        if (!"ADMIN".equalsIgnoreCase(role) && !"MANAGER".equalsIgnoreCase(role) 
                && !booking.getUserId().equals(userId)) {
            throw new AccessDeniedException("You can only view your own bookings");
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Booking retrieved successfully",
                "data", booking
        ));
    }

    /**
     * Get all bookings for current user.
     */
    @GetMapping("/my-bookings")
    public ResponseEntity<?> getMyBookings(@RequestHeader("X-User-Id") Long userId) {
        List<BookingResponse> bookings = bookingService.getMyBookings(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Bookings retrieved successfully",
                "data", bookings
        ));
    }

    /**
     * Get all bookings for a specific hotel (staff only).
     */
    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<?> getBookingsByHotel(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader(value = "X-Hotel-Id", required = false) Long userHotelId,
            @PathVariable Long hotelId
    ) {
        // Only staff can view hotel bookings
        if (!"ADMIN".equalsIgnoreCase(role) && !"MANAGER".equalsIgnoreCase(role) 
                && !"RECEPTIONIST".equalsIgnoreCase(role)) {
            throw new AccessDeniedException("Only staff can view hotel bookings");
        }

        // Context-aware: Staff can only view bookings for their own hotel
        if (!"ADMIN".equalsIgnoreCase(role)) {
            if (userHotelId == null) {
                throw new ValidationException("User must be assigned to a hotel");
            }
            if (!hotelId.equals(userHotelId)) {
                throw new AccessDeniedException("You can only view bookings for your assigned hotel");
            }
        }

        List<BookingResponse> bookings = bookingService.getBookingsByHotel(hotelId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Hotel bookings retrieved successfully",
                "data", bookings
        ));
    }

    /**
     * Get all bookings (admin only).
     */
    @GetMapping
    public ResponseEntity<?> getAllBookings(@RequestHeader("X-User-Role") String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new AccessDeniedException("Only ADMIN can view all bookings");
        }

        List<BookingResponse> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "All bookings retrieved successfully",
                "data", bookings
        ));
    }

    /**
     * Confirm a booking (simulates payment completion).
     * In production, this would be called by Payment Service after successful payment.
     * For testing purposes, ADMIN/MANAGER can manually confirm bookings.
     */
    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<?> confirmBooking(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long bookingId
    ) {
        // Only ADMIN or MANAGER can manually confirm bookings (for testing)
        // In production, Payment Service would call this internally
        if (!"ADMIN".equalsIgnoreCase(role) && !"MANAGER".equalsIgnoreCase(role)) {
            throw new AccessDeniedException("Only ADMIN or MANAGER can confirm bookings");
        }

        BookingResponse booking = bookingService.confirmBooking(bookingId);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Booking confirmed successfully. Bill will be generated automatically.",
                "data", booking
        ));
    }

    /**
     * Cancel a booking.
     */
    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<?> cancelBooking(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long bookingId,
            @Valid @RequestBody CancelBookingRequest request
    ) {
        // GUEST can cancel their own bookings, ADMIN can cancel any
        BookingResponse booking = bookingService.cancelBooking(bookingId, userId, role, request);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Booking cancelled successfully",
                "data", booking
        ));
    }

    /**
     * Check-in guest.
     * Staff only (MANAGER, RECEPTIONIST, ADMIN).
     */
    @PostMapping("/{bookingId}/check-in")
    public ResponseEntity<?> checkIn(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader(value = "X-Hotel-Id", required = false) Long hotelId,
            @PathVariable Long bookingId,
            @RequestBody(required = false) CheckInRequest request
    ) {
        // Only staff can check in guests
        if (!"ADMIN".equalsIgnoreCase(role) && !"MANAGER".equalsIgnoreCase(role) 
                && !"RECEPTIONIST".equalsIgnoreCase(role)) {
            throw new AccessDeniedException("Only staff can check in guests");
        }

        if (request == null) {
            request = new CheckInRequest();
        }

        BookingResponse booking = bookingService.checkIn(bookingId, hotelId, request);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Guest checked in successfully",
                "data", booking
        ));
    }

    /**
     * Check-out guest.
     * Staff only (MANAGER, RECEPTIONIST, ADMIN).
     */
    @PostMapping("/{bookingId}/check-out")
    public ResponseEntity<?> checkOut(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader(value = "X-Hotel-Id", required = false) Long hotelId,
            @PathVariable Long bookingId,
            @RequestBody(required = false) CheckOutRequest request
    ) {
        // Only staff can check out guests
        if (!"ADMIN".equalsIgnoreCase(role) && !"MANAGER".equalsIgnoreCase(role) 
                && !"RECEPTIONIST".equalsIgnoreCase(role)) {
            throw new AccessDeniedException("Only staff can check out guests");
        }

        if (request == null) {
            request = new CheckOutRequest();
        }

        BookingResponse booking = bookingService.checkOut(bookingId, hotelId, request);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Guest checked out successfully",
                "data", booking
        ));
    }

    /**
     * Get today's check-ins for a hotel.
     * Staff only.
     */
    @GetMapping("/hotel/{hotelId}/today-checkins")
    public ResponseEntity<?> getTodayCheckIns(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader(value = "X-Hotel-Id", required = false) Long userHotelId,
            @PathVariable Long hotelId
    ) {
        // Only staff can view today's check-ins
        if (!"ADMIN".equalsIgnoreCase(role) && !"MANAGER".equalsIgnoreCase(role) 
                && !"RECEPTIONIST".equalsIgnoreCase(role)) {
            throw new AccessDeniedException("Only staff can view today's check-ins");
        }

        // Context-aware: Staff can only view for their own hotel
        if (!"ADMIN".equalsIgnoreCase(role)) {
            if (userHotelId == null) {
                throw new ValidationException("User must be assigned to a hotel");
            }
            if (!hotelId.equals(userHotelId)) {
                throw new AccessDeniedException("You can only view check-ins for your assigned hotel");
            }
        }

        List<BookingResponse> bookings = bookingService.getTodayCheckIns(hotelId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Today's check-ins retrieved successfully",
                "data", bookings
        ));
    }

    /**
     * Get today's check-outs for a hotel.
     * Staff only.
     */
    @GetMapping("/hotel/{hotelId}/today-checkouts")
    public ResponseEntity<?> getTodayCheckOuts(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader(value = "X-Hotel-Id", required = false) Long userHotelId,
            @PathVariable Long hotelId
    ) {
        // Only staff can view today's check-outs
        if (!"ADMIN".equalsIgnoreCase(role) && !"MANAGER".equalsIgnoreCase(role) 
                && !"RECEPTIONIST".equalsIgnoreCase(role)) {
            throw new AccessDeniedException("Only staff can view today's check-outs");
        }

        // Context-aware: Staff can only view for their own hotel
        if (!"ADMIN".equalsIgnoreCase(role)) {
            if (userHotelId == null) {
                throw new ValidationException("User must be assigned to a hotel");
            }
            if (!hotelId.equals(userHotelId)) {
                throw new AccessDeniedException("You can only view check-outs for your assigned hotel");
            }
        }

        List<BookingResponse> bookings = bookingService.getTodayCheckOuts(hotelId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Today's check-outs retrieved successfully",
                "data", bookings
        ));
    }
}

