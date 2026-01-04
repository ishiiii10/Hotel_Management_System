package com.hotelbooking.booking.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotelbooking.booking.dto.response.BookingResponse;
import com.hotelbooking.booking.service.BookingService;

import lombok.RequiredArgsConstructor;

/**
 * Internal controller for inter-service communication via Feign.
 * Returns raw data without wrapper objects for easier deserialization.
 */
@RestController
@RequestMapping("/internal/bookings")
@RequiredArgsConstructor
public class InternalBookingController {

    private final BookingService bookingService;

    @GetMapping("/{bookingId}")
    public BookingResponse getBookingById(@PathVariable Long bookingId) {
        return bookingService.getBookingById(bookingId);
    }
}

