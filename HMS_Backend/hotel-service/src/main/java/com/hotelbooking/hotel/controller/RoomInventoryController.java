package com.hotelbooking.hotel.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.hotelbooking.hotel.domain.RoomInventory;
import com.hotelbooking.hotel.dto.*;
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
            throw new IllegalStateException("Missing role information");
        }
        if (!"ADMIN".equals(role) && !"MANAGER".equals(role)) {
            throw new IllegalStateException("Access denied");
        }
    }

    private void verifyHotel(Long staffHotelId, Long hotelId) {
        if (!staffHotelId.equals(hotelId)) {
            throw new IllegalStateException("Cross-hotel access denied");
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