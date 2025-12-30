package com.hotelbooking.hotel.controller;

import java.time.LocalDate;

import org.springframework.web.bind.annotation.*;

import com.hotelbooking.hotel.dto.AvailabilityResponse;
import com.hotelbooking.hotel.service.AvailabilityService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping("/hotels/availability")
    public AvailabilityResponse checkAvailability(
            @RequestParam Long hotelId,
            @RequestParam Long categoryId,
            @RequestParam LocalDate checkInDate,
            @RequestParam LocalDate checkOutDate
    ) {
        int available = availabilityService.getAvailability(
                hotelId,
                categoryId,
                checkInDate,
                checkOutDate
        );

        return new AvailabilityResponse(
                hotelId,
                categoryId,
                checkInDate,
                checkOutDate,
                available
        );
    }
}