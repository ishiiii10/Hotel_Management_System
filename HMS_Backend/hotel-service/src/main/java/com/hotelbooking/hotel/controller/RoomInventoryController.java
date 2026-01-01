package com.hotelbooking.hotel.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.hotelbooking.hotel.domain.RoomInventory;
import com.hotelbooking.hotel.dto.RoomInventoryResponse;
import com.hotelbooking.hotel.dto.UpdateInventoryRequest;
import com.hotelbooking.hotel.service.RoomInventoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RoomInventoryController {

    private final RoomInventoryService service;

    /* ---------------- ADMIN / MANAGER ---------------- */

    @PutMapping("/hotels/{hotelId}/inventory")
    public RoomInventoryResponse updateInventory(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-Hotel-Id", required = false) Long staffHotelId,
            @PathVariable Long hotelId,
            @Valid @RequestBody UpdateInventoryRequest request
    ) {
        requireAdminOrManager(role);

        // Only MANAGER is hotel-bound
        if ("MANAGER".equals(role)) {
            if (staffHotelId == null) {
                throw new IllegalStateException("Missing hotel context for manager");
            }
            verifyHotel(staffHotelId, hotelId);
        }

        RoomInventory inventory = service.upsertInventory(
                hotelId,
                request.getCategoryId(),
                request.getTotalRooms()
        );

        return toResponse(inventory);
    }

    /* ---------------- ADMIN / MANAGER ---------------- */

    @GetMapping("/hotels/{hotelId}/inventory")
    public List<RoomInventoryResponse> getInventory(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-Hotel-Id", required = false) Long staffHotelId,
            @PathVariable Long hotelId
    ) {
        requireAdminOrManager(role);

        // Manager can only view own hotel
        if ("MANAGER".equals(role)) {
            if (staffHotelId == null) {
                throw new IllegalStateException("Missing hotel context");
            }
            verifyHotel(staffHotelId, hotelId);
        }

        return service.getInventory(hotelId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /* ---------------- Helpers ---------------- */

    

    private void requireAdminOrManager(String role) {
        if (role == null) {
            throw new com.hotelbooking.hotel.exception.HotelException(
                com.hotelbooking.hotel.exception.HotelErrorCode.VALIDATION_ERROR,
                "Missing role information",
                org.springframework.http.HttpStatus.BAD_REQUEST
            );
        }
        if (!"ADMIN".equals(role) && !"MANAGER".equals(role)) {
            throw new com.hotelbooking.hotel.exception.HotelException(
                com.hotelbooking.hotel.exception.HotelErrorCode.ACCESS_DENIED,
                "Access denied",
                org.springframework.http.HttpStatus.FORBIDDEN
            );
        }
    }

    private void verifyHotel(Long staffHotelId, Long hotelId) {
        if (!staffHotelId.equals(hotelId)) {
            throw new com.hotelbooking.hotel.exception.HotelException(
                com.hotelbooking.hotel.exception.HotelErrorCode.ACCESS_DENIED,
                "Cross-hotel access denied",
                org.springframework.http.HttpStatus.FORBIDDEN
            );
        }
    }

    private RoomInventoryResponse toResponse(RoomInventory i) {
        return new RoomInventoryResponse(
                i.getHotelId(),
                i.getCategoryId(),
                i.getTotalRooms(),
                i.getOutOfService(),
                i.getAvailableRooms()
        );
    }
}