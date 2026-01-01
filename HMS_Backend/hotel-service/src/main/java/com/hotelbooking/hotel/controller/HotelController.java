package com.hotelbooking.hotel.controller;



import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hotelbooking.hotel.domain.City;
import com.hotelbooking.hotel.domain.Hotel;
import com.hotelbooking.hotel.dto.CreateHotelRequest;
import com.hotelbooking.hotel.dto.HotelListResponse;
import com.hotelbooking.hotel.dto.HotelResponse;
import com.hotelbooking.hotel.dto.UpdateHotelRequest;
import com.hotelbooking.hotel.service.HotelService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    /* ---------------- ADMIN APIs ---------------- */

    @PostMapping
    public ResponseEntity<HotelResponse> createHotel(
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody CreateHotelRequest request
    ) {
        requireAdmin(role);

        Hotel hotel = hotelService.createHotel(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toHotelResponse(hotel));
    }

    @PutMapping("/{hotelId}")
    public ResponseEntity<HotelResponse> updateHotel(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long hotelId,
            @Valid @RequestBody UpdateHotelRequest request
    ) {
        requireAdmin(role);

        return ResponseEntity.ok(
                toHotelResponse(hotelService.updateHotel(hotelId, request))
        );
    }

    @DeleteMapping("/{hotelId}")
    public ResponseEntity<Void> disableHotel(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long hotelId
    ) {
        requireAdmin(role);
        hotelService.disableHotel(hotelId);
        return ResponseEntity.noContent().build();
    }

    /* ---------------- PUBLIC APIs ---------------- */

    @GetMapping
    public ResponseEntity<List<HotelListResponse>> listHotels(
            @RequestParam(required = false) City city
    ) {
        return ResponseEntity.ok(
                hotelService.listHotels(city)
                        .stream()
                        .map(this::toHotelListResponse)
                        .toList()
        );
    } // No auth headers required


    @GetMapping("/{hotelId}")
    public ResponseEntity<HotelResponse> getHotel(
            @PathVariable Long hotelId
    ) {
        return ResponseEntity.ok(
                toHotelResponse(hotelService.getHotel(hotelId))
        );
    } // No auth headers required


    /* ---------------- Helpers ---------------- */

    private void requireAdmin(String role) {
        if (!"ADMIN".equals(role)) {
            throw new com.hotelbooking.hotel.exception.HotelException(
                com.hotelbooking.hotel.exception.HotelErrorCode.ACCESS_DENIED,
                "Access denied",
                org.springframework.http.HttpStatus.FORBIDDEN
            );
        }
    }

    private HotelResponse toHotelResponse(Hotel hotel) {
        return new HotelResponse(
                hotel.getId(),
                hotel.getName(),
                hotel.getCity(),
                hotel.getAddress(),
                hotel.getEmail(),
                hotel.getPhoneNumber(),
                hotel.getCategory(),
                hotel.getStatus()
        );
    }

    private HotelListResponse toHotelListResponse(Hotel hotel) {
        return new HotelListResponse(
                hotel.getId(),
                hotel.getName(),
                hotel.getCity(),
                hotel.getEmail(),
                hotel.getPhoneNumber(),
                hotel.getCategory(),
                hotel.getStatus()
        );
    }
}