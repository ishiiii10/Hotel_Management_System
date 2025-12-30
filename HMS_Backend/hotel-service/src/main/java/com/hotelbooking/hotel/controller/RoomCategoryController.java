package com.hotelbooking.hotel.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.hotelbooking.hotel.domain.RoomCategory;
import com.hotelbooking.hotel.dto.*;
import com.hotelbooking.hotel.service.RoomCategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RoomCategoryController {

    private final RoomCategoryService service;

    @PostMapping("/hotels/{hotelId}/room-categories")
    public RoomCategoryResponse create(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader(value = "X-Hotel-Id", required = false) Long staffHotelId,
            @PathVariable Long hotelId,
            @Valid @RequestBody CreateRoomCategoryRequest request
    ) {
        requireAdminOrManager(role, staffHotelId, hotelId);

        RoomCategory category = RoomCategory.builder()
                .name(request.getName())
                .basePrice(request.getBasePrice())
                .maxOccupancy(request.getMaxOccupancy())
                .build();

        return toResponse(service.create(hotelId, category));
    }

    @PutMapping("/room-categories/{categoryId}")
    public RoomCategoryResponse update(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader(value = "X-Hotel-Id", required = false) Long staffHotelId,
            @PathVariable Long categoryId,
            @Valid @RequestBody UpdateRoomCategoryRequest request
    ) {
        RoomCategory category = service.get(categoryId);
        requireAdminOrManager(role, staffHotelId, category.getHotelId());

        category.setBasePrice(request.getBasePrice());
        category.setMaxOccupancy(request.getMaxOccupancy());
        category.setActive(request.isActive());

        return toResponse(service.update(category));
    }

    @GetMapping("/hotels/{hotelId}/room-categories")
    public List<RoomCategoryResponse> list(@PathVariable Long hotelId) {
        return service.listByHotel(hotelId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/room-categories/{categoryId}")
    public RoomCategoryResponse get(@PathVariable Long categoryId) {
        return toResponse(service.get(categoryId));
    }

    /* ---------------- Helpers ---------------- */

    private void requireAdminOrManager(
            String role,
            Long staffHotelId,
            Long targetHotelId
    ) {
        if ("ADMIN".equals(role)) return;

        if ("MANAGER".equals(role) && staffHotelId != null && staffHotelId.equals(targetHotelId)) {
            return;
        }

        throw new IllegalStateException("Access denied");
    }

    private RoomCategoryResponse toResponse(RoomCategory c) {
        return new RoomCategoryResponse(
                c.getId(),
                c.getHotelId(),
                c.getName(),
                c.getBasePrice(),
                c.getMaxOccupancy(),
                c.isActive()
        );
    }
}