package com.hotelbooking.hotel.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
            throw new com.hotelbooking.hotel.exception.HotelException(
                com.hotelbooking.hotel.exception.HotelErrorCode.VALIDATION_ERROR,
                "Invalid date range",
                org.springframework.http.HttpStatus.BAD_REQUEST
            );
        }

        return searchService.search(city, checkInDate, checkOutDate);
    }
}