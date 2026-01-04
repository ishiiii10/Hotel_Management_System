package com.hotelbooking.billing.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.hotelbooking.billing.dto.BookingInfoResponse;

@FeignClient(name = "BOOKING-SERVICE", path = "/internal/bookings")
public interface BookingServiceClient {

    @GetMapping("/{bookingId}")
    BookingInfoResponse getBookingById(@PathVariable Long bookingId);
}

