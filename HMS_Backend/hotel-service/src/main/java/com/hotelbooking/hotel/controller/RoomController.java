package com.hotelbooking.hotel.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
            @RequestHeader(value = "X-Hotel-Id", required = false) Long userHotelId,
            @Valid @RequestBody CreateRoomRequest request
    ) {
        authorize(role);
        
        // Context-aware authorization: Staff can only create rooms in their assigned hotel
        if (userHotelId == null && !"ADMIN".equalsIgnoreCase(role)) {
            throw new IllegalStateException("User must be assigned to a hotel");
        }
        
        if (!"ADMIN".equalsIgnoreCase(role) && !request.getHotelId().equals(userHotelId)) {
            throw new IllegalStateException("Forbidden: Cannot create room for another hotel");
        }

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
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getRoomById(@PathVariable Long id) {

        RoomResponse response = roomService.getRoomById(id);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", response
        ));
    }

    /* -------- DELETE ROOM (SOFT DELETE) -------- */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoom(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader(value = "X-Hotel-Id", required = false) Long userHotelId,
            @PathVariable Long id
    ) {
        authorize(role);
        
        // Context-aware authorization: Staff can only delete rooms in their assigned hotel
        if (userHotelId == null && !"ADMIN".equalsIgnoreCase(role)) {
            throw new IllegalStateException("User must be assigned to a hotel");
        }
        
        RoomResponse room = roomService.getRoomById(id);
        if (!"ADMIN".equalsIgnoreCase(role) && !room.getHotelId().equals(userHotelId)) {
            throw new IllegalStateException("Forbidden: Cannot delete room of another hotel");
        }

        roomService.deleteRoom(id);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Room deleted successfully"
        ));
    }


    
    //Update
    @PutMapping("/{roomId}")
    public ResponseEntity<?> updateRoom(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader(value = "X-Hotel-Id", required = false) Long userHotelId,
            @PathVariable Long roomId,
            @Valid @RequestBody CreateRoomRequest request
    ) {
        authorize(role);
        
        // Context-aware authorization: Staff can only modify rooms in their assigned hotel
        if (userHotelId == null && !"ADMIN".equalsIgnoreCase(role)) {
            throw new IllegalStateException("User must be assigned to a hotel");
        }
        
        // Fetch room to check hotelId
        RoomResponse room = roomService.getRoomById(roomId);
        if (!"ADMIN".equalsIgnoreCase(role) && !room.getHotelId().equals(userHotelId)) {
            throw new IllegalStateException("Forbidden: Cannot modify room of another hotel");
        }

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
            @RequestHeader(value = "X-Hotel-Id", required = false) Long userHotelId,
            @PathVariable Long roomId,
            @RequestParam RoomStatus status
    ) {
        authorizeStatusChange(role);
        
        // Context-aware authorization: Staff can only modify rooms in their assigned hotel
        if (userHotelId == null && !"ADMIN".equalsIgnoreCase(role)) {
            throw new IllegalStateException("User must be assigned to a hotel");
        }
        
        RoomResponse room = roomService.getRoomById(roomId);
        if (!"ADMIN".equalsIgnoreCase(role) && !room.getHotelId().equals(userHotelId)) {
            throw new IllegalStateException("Forbidden: Cannot modify room of another hotel");
        }

        roomService.updateRoomStatus(roomId, status);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Room status updated successfully"
        ));
    }

    @PatchMapping("/{roomId}/active")
    public ResponseEntity<?> updateRoomActiveStatus(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader(value = "X-Hotel-Id", required = false) Long userHotelId,
            @PathVariable Long roomId,
            @RequestParam boolean active
    ) {
        authorize(role);
        
        // Context-aware authorization: Staff can only modify rooms in their assigned hotel
        if (userHotelId == null && !"ADMIN".equalsIgnoreCase(role)) {
            throw new IllegalStateException("User must be assigned to a hotel");
        }
        
        RoomResponse room = roomService.getRoomById(roomId);
        if (!"ADMIN".equalsIgnoreCase(role) && !room.getHotelId().equals(userHotelId)) {
            throw new IllegalStateException("Forbidden: Cannot modify room of another hotel");
        }

        roomService.updateRoomActiveStatus(roomId, active);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Room activation status updated successfully"
        ));
    }

    private void authorize(String role) {
        if (!role.equalsIgnoreCase("ADMIN") && !role.equalsIgnoreCase("MANAGER")) {
            throw new IllegalStateException("Only ADMIN or MANAGER allowed");
        }
    }

    private void authorizeStatusChange(String role) {
        if (!role.equalsIgnoreCase("ADMIN") && !role.equalsIgnoreCase("MANAGER") && !role.equalsIgnoreCase("RECEPTIONIST")) {
            throw new IllegalStateException("Only ADMIN, MANAGER, or RECEPTIONIST allowed to change room status");
        }
    }
}