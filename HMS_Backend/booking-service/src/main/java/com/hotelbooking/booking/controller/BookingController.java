package com.hotelbooking.booking.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hotelbooking.booking.domain.Booking;
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

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BookingResponse(
                        booking.getBookingCode(),
                        booking.getHotelId(),
                        booking.getCategoryId(),
                        booking.getCheckInDate(),
                        booking.getCheckOutDate(),
                        booking.getStatus()
                ));
    }
}