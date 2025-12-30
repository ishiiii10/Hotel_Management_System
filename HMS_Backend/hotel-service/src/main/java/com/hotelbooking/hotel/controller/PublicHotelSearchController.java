package com.hotelbooking.hotel.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.hotelbooking.hotel.domain.City;
import com.hotelbooking.hotel.dto.PublicHotelSearchResponse;
import com.hotelbooking.hotel.service.PublicHotelSearchService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public/hotels")
public class PublicHotelSearchController {

    private final PublicHotelSearchService searchService;

    @GetMapping("/search")
    public List<PublicHotelSearchResponse> search(
            @RequestParam City city,
            @RequestParam LocalDate checkInDate,
            @RequestParam LocalDate checkOutDate
    ) {
        if (!checkOutDate.isAfter(checkInDate)) {
            throw new IllegalArgumentException("Invalid date range");
        }

        return searchService.search(city, checkInDate, checkOutDate);
    }
}