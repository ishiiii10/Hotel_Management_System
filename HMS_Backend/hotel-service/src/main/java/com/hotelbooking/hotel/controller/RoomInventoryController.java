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

    /* ---------------- MANAGER ---------------- */

    @PutMapping("/hotels/{hotelId}/inventory")
    public RoomInventoryResponse updateInventory(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-Hotel-Id") Long staffHotelId,
            @PathVariable Long hotelId,
            @Valid @RequestBody UpdateInventoryRequest request
    ) {
        requireManager(role);
        verifyHotel(staffHotelId, hotelId);

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
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long hotelId
    ) {
        requireAdminOrManager(role);

        return service.getInventory(hotelId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /* ---------------- Helpers ---------------- */

    private void requireManager(String role) {
        if (!"MANAGER".equals(role)) {
            throw new IllegalStateException("Access denied");
        }
    }

    private void requireAdminOrManager(String role) {
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