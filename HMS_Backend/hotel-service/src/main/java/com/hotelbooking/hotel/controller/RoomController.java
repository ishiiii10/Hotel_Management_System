package com.hotelbooking.hotel.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hotelbooking.hotel.dto.request.CreateRoomRequest;
import com.hotelbooking.hotel.dto.response.RoomResponse;
import com.hotelbooking.hotel.enums.RoomStatus;
import com.hotelbooking.hotel.service.RoomService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/hotels/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    
    //Create
    @PostMapping
    public ResponseEntity<?> createRoom(
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody CreateRoomRequest request
    ) {
        authorize(role);

        Long id = roomService.createRoom(request);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Room created successfully",
                "data", Map.of("id", id)
        ));
    }

    
    //Read
    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<?> getRoomsByHotel(@PathVariable Long hotelId) {

        List<RoomResponse> rooms = roomService.getRoomsByHotel(hotelId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", rooms
        ));
    }

    
    //Update
    @PutMapping("/{roomId}")
    public ResponseEntity<?> updateRoom(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long roomId,
            @Valid @RequestBody CreateRoomRequest request
    ) {
        authorize(role);

        Long id = roomService.updateRoom(roomId, request);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Room updated successfully",
                "data", Map.of("id", id)
        ));
    }

    
    //Status
    @PatchMapping("/{roomId}/status")
    public ResponseEntity<?> updateRoomStatus(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long roomId,
            @RequestParam RoomStatus status
    ) {
        authorize(role);

        roomService.updateRoomStatus(roomId, status);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Room status updated successfully"
        ));
    }

    @PatchMapping("/{roomId}/active")
    public ResponseEntity<?> updateRoomActiveStatus(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long roomId,
            @RequestParam boolean active
    ) {
        authorize(role);

        roomService.updateRoomActiveStatus(roomId, active);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Room activation status updated successfully"
        ));
    }

    private void authorize(String role) {
        if (!role.equalsIgnoreCase("ADMIN")
                && !role.equalsIgnoreCase("MANAGER")) {
            throw new IllegalStateException("Only ADMIN or MANAGER allowed");
        }
    }
}