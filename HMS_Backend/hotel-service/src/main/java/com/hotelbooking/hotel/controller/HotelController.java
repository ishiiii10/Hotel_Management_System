package com.hotelbooking.hotel.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hotelbooking.hotel.dto.request.CreateHotelRequest;
import com.hotelbooking.hotel.dto.request.CreateStaffRequest;
import com.hotelbooking.hotel.dto.request.CreateStaffRequestBody;
import com.hotelbooking.hotel.dto.response.HotelDetailResponse;
import com.hotelbooking.hotel.dto.response.HotelSearchResponse;
import com.hotelbooking.hotel.enums.City;
import com.hotelbooking.hotel.enums.Hotel_Category;
import com.hotelbooking.hotel.feign.AuthServiceClient;
import com.hotelbooking.hotel.repository.HotelRepository;
import com.hotelbooking.hotel.service.HotelService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelController {

    @GetMapping("/my-hotel")
    public ResponseEntity<?> getMyHotel(@RequestHeader(value = "X-Hotel-Id", required = false) Long userHotelId) {
        if (userHotelId == null) {
            throw new IllegalStateException("User must be assigned to a hotel");
        }
        HotelDetailResponse hotel = hotelService.getHotelById(userHotelId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Hotel details retrieved successfully",
                "data", hotel
        ));
    }

    private final HotelService hotelService;
    private final HotelRepository hotelRepository;
    private final AuthServiceClient authServiceClient;

    

    
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
            @RequestHeader(value = "X-Hotel-Id", required = false) Long userHotelId,
            @PathVariable Long hotelId,
            @Valid @RequestBody CreateHotelRequest request
    ) {

        if (!"ADMIN".equalsIgnoreCase(role) && !"MANAGER".equalsIgnoreCase(role)) {
            throw new IllegalStateException("Only ADMIN or MANAGER can update hotels");
        }

        // Context-aware authorization: MANAGER can only update their own hotel
        if ("MANAGER".equalsIgnoreCase(role)) {
            if (userHotelId == null) {
                throw new IllegalStateException("MANAGER must be assigned to a hotel");
            }
            if (!hotelId.equals(userHotelId)) {
                throw new IllegalStateException("Forbidden: MANAGER can only update their assigned hotel");
            }
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

    /**
     * Create staff user for a hotel.
     * Only ADMIN can create staff.
     * Hotel Service validates hotel exists, then calls Auth Service via Feign.
     */
    @PostMapping("/{hotelId}/staff")
    public ResponseEntity<?> createStaff(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long hotelId,
            @Valid @RequestBody CreateStaffRequestBody requestBody
    ) {
        // Only ADMIN can create staff
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new IllegalStateException("Only ADMIN can create staff");
        }

        // Validate hotel exists
        if (!hotelRepository.existsById(hotelId)) {
            throw new IllegalStateException("Hotel not found: " + hotelId);
        }

        // Build CreateStaffRequest with hotelId from path variable
        CreateStaffRequest request = new CreateStaffRequest();
        request.setFullName(requestBody.getFullName());
        request.setUsername(requestBody.getUsername());
        request.setEmail(requestBody.getEmail());
        request.setPassword(requestBody.getPassword());
        request.setRole(requestBody.getRole());
        request.setHotelId(hotelId);

        // Call Auth Service via Feign to create staff user
        Map<String, String> response = authServiceClient.createStaff(request);

        return ResponseEntity.status(201).body(Map.of(
                "success", true,
                "message", "Staff user created successfully",
                "data", response
        ));
    }
}