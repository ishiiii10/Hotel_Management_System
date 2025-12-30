package com.hotelbooking.hotel.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hotelbooking.hotel.domain.PhysicalRoom;
import com.hotelbooking.hotel.dto.*;
import com.hotelbooking.hotel.service.PhysicalRoomService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PhysicalRoomController {

    private final PhysicalRoomService service;

    /* ---------------- ADMIN ---------------- */

    @PostMapping("/hotels/{hotelId}/physical-rooms")
    public ResponseEntity<List<PhysicalRoomResponse>> registerRooms(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long hotelId,
            @Valid @RequestBody RegisterPhysicalRoomsRequest request
    ) {
        requireAdmin(role);

        List<PhysicalRoom> rooms = request.getRooms().stream()
                .map(r -> PhysicalRoom.builder()
                        .roomNumber(r.getRoomNumber())
                        .floor(r.getFloor())
                        .build())
                .toList();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.registerRooms(
                        hotelId,
                        request.getCategoryId(),
                        rooms
                ).stream().map(this::toResponse).toList());
    }

    /* ---------------- ALL ---------------- */

    @GetMapping("/physical-rooms/{roomId}")
    public PhysicalRoomResponse getRoom(@PathVariable Long roomId) {
        return toResponse(service.getRoom(roomId));
    }

    /* ---------------- RECEPTIONIST ---------------- */

    @PutMapping("/physical-rooms/{roomId}/assign")
    public PhysicalRoomResponse assignRoom(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-Hotel-Id") Long staffHotelId,
            @PathVariable Long roomId,
            @Valid @RequestBody AssignRoomRequest request
    ) {
        requireReceptionist(role);
        verifyHotelOwnership(staffHotelId, service.getRoom(roomId).getHotelId());
        return toResponse(service.assignRoom(roomId));
    }

    @PutMapping("/physical-rooms/{roomId}/release")
    public PhysicalRoomResponse releaseRoom(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-Hotel-Id") Long staffHotelId,
            @PathVariable Long roomId
    ) {
        requireReceptionist(role);
        verifyHotelOwnership(staffHotelId, service.getRoom(roomId).getHotelId());
        return toResponse(service.releaseRoom(roomId));
    }

    /* ---------------- MANAGER ---------------- */

    @PutMapping("/physical-rooms/{roomId}/maintenance")
    public PhysicalRoomResponse maintenance(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-Hotel-Id") Long staffHotelId,
            @PathVariable Long roomId,
            @RequestBody MaintenanceRequest request
    ) {
        requireManager(role);
        verifyHotelOwnership(staffHotelId, service.getRoom(roomId).getHotelId());
        return toResponse(
                service.updateMaintenance(roomId, request.isUnderMaintenance())
        );
    }

    /* ---------------- Helpers ---------------- */

    private void requireAdmin(String role) {
        if (!"ADMIN".equals(role)) {
            throw new IllegalStateException("Access denied");
        }
    }

    private void requireReceptionist(String role) {
        if (!"RECEPTIONIST".equals(role)) {
            throw new IllegalStateException("Access denied");
        }
    }

    private void requireManager(String role) {
        if (!"MANAGER".equals(role)) {
            throw new IllegalStateException("Access denied");
        }
    }

    private void verifyHotelOwnership(Long staffHotelId, Long roomHotelId) {
        if (!staffHotelId.equals(roomHotelId)) {
            throw new IllegalStateException("Cross-hotel access denied");
        }
    }

    private PhysicalRoomResponse toResponse(PhysicalRoom r) {
        return new PhysicalRoomResponse(
                r.getId(),
                r.getHotelId(),
                r.getCategoryId(),
                r.getRoomNumber(),
                r.getState(),
                r.getFloor()
        );
    }
}