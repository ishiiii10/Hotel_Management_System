package com.hotelbooking.hotel.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hotelbooking.hotel.domain.City;
import com.hotelbooking.hotel.domain.Hotel_Category;
import com.hotelbooking.hotel.dto.request.CreateHotelRequest;
import com.hotelbooking.hotel.dto.response.HotelDetailResponse;
import com.hotelbooking.hotel.dto.response.HotelSearchResponse;
import com.hotelbooking.hotel.service.HotelService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    

    
    @GetMapping("/search")
    public ResponseEntity<?> searchHotelsByCity(
            @RequestParam(required = false) City city,
            @RequestParam(required = false) Hotel_Category category
    ) {

        List<HotelSearchResponse> response;

        if (city != null) {
            response = hotelService.searchHotelsByCity(city);
        } else if (category != null) {
            response = hotelService.searchHotelsByCategory(category);
        } else {
            throw new IllegalArgumentException("Either city or category must be provided");
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Hotels search completed successfully",
                "data", response
        ));
    }

   
    @GetMapping("/{hotelId}")
    public ResponseEntity<?> getHotelById(@PathVariable Long hotelId) {

        HotelDetailResponse hotel = hotelService.getHotelById(hotelId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Hotel details retrieved successfully",
                "data", hotel
        ));
    }

   
    
    @PostMapping
    public ResponseEntity<?> createHotel(
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody CreateHotelRequest request
    ) {

        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new IllegalStateException("Only ADMIN can create hotels");
        }

        Long id = hotelService.createHotel(request);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Hotel created successfully",
                "data", Map.of("id", id)
        ));
    }

    
    @PutMapping("/{hotelId}")
    public ResponseEntity<?> updateHotel(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long hotelId,
            @Valid @RequestBody CreateHotelRequest request
    ) {

        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new IllegalStateException("Only ADMIN can update hotels");
        }

        Long id = hotelService.updateHotel(hotelId, request);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Hotel updated successfully",
                "data", Map.of("id", id)
        ));
    }

    
    @GetMapping
    public ResponseEntity<?> getAllHotels(
            @RequestHeader("X-User-Role") String role
    ) {

        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new IllegalStateException("Only ADMIN can view all hotels");
        }

        List<HotelDetailResponse> hotels = hotelService.getAllHotels();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", hotels
        ));
    }
}