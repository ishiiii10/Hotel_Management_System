package com.hotelbooking.booking.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hotelbooking.booking.domain.Booking;
import com.hotelbooking.booking.domain.BookingGuest;
import com.hotelbooking.booking.dto.BookingGuestResponse;
import com.hotelbooking.booking.dto.BookingResponse;
import com.hotelbooking.booking.dto.CreateBookingRequest;
import com.hotelbooking.booking.service.BookingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody CreateBookingRequest request
    ) {
        Booking booking = bookingService.createBooking(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(booking));
    }

    @GetMapping("/{id}")
    public BookingResponse getBooking(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role
    ) {
        return toResponse(bookingService.getBookingById(id, userId, role));
    }

    @GetMapping("/my-bookings")
    public List<BookingResponse> myBookings(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role
    ) {
        requireGuest(role);
        return bookingService.getBookingsForGuest(userId)
                .stream().map(this::toResponse).toList();
    }

    @GetMapping("/hotel/{hotelId}")
    public List<BookingResponse> hotelBookings(
            @PathVariable Long hotelId,
            @RequestHeader("X-User-Role") String role,
            @RequestHeader(value = "X-Hotel-Id", required = false) Long staffHotelId
    ) {
        requireAdminOrManager(role);
        return bookingService.getBookingsForHotel(hotelId, staffHotelId, role)
                .stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}/guests")
    public List<BookingGuestResponse> getBookingGuests(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role
    ) {
        return bookingService.getGuestsForBooking(id, userId, role)
                .stream().map(this::toGuestResponse).toList();
    }

    /* ---------- Helpers ---------- */

    private void requireGuest(String role) {
        if (!"GUEST".equals(role)) {
            throw new IllegalStateException("Access denied");
        }
    }

    private void requireAdminOrManager(String role) {
        if (!"ADMIN".equals(role) && !"MANAGER".equals(role)) {
            throw new IllegalStateException("Access denied");
        }
    }

    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getBookingCode(),
                booking.getHotelId(),
                booking.getCategoryId(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getStatus()
        );
    }

    private BookingGuestResponse toGuestResponse(BookingGuest guest) {
        return new BookingGuestResponse(
                guest.getFullName(),
                guest.getAge(),
                guest.getIdType(),
                guest.getIdNumber()
        );
    }
}