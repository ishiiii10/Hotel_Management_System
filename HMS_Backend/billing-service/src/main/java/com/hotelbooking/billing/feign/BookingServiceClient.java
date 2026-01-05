package com.hotelbooking.billing.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.hotelbooking.billing.dto.BookingInfoResponse;

@FeignClient(name = "BOOKING-SERVICE", path = "/internal/bookings")
public interface BookingServiceClient {

    @GetMapping("/{bookingId}")
    BookingInfoResponse getBookingById(@PathVariable Long bookingId);

    /**
     * Confirm a booking (internal endpoint for billing service)
     * Called when walk-in booking bill is marked as paid
     */
    @PostMapping("/{bookingId}/confirm")
    BookingInfoResponse confirmBooking(@PathVariable Long bookingId);
}

