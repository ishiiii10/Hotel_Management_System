package com.hotelbooking.hotel.controller;



import java.time.LocalDate;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hotelbooking.hotel.dto.request.BlockRoomRequest;
import com.hotelbooking.hotel.dto.request.UnblockRoomRequest;
import com.hotelbooking.hotel.service.RoomAvailabilityService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/hotels/availability")
@RequiredArgsConstructor
public class RoomAvailabilityController {

    private final RoomAvailabilityService availabilityService;

    
    @PostMapping("/block")
    public ResponseEntity<?> blockRoom(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader(value = "X-Hotel-Id", required = false) Long userHotelId,
            @Valid @RequestBody BlockRoomRequest request
    ) {
        if (!role.equalsIgnoreCase("ADMIN")
                && !role.equalsIgnoreCase("MANAGER")) {
            throw new IllegalStateException("Only ADMIN or MANAGER can block rooms");
        }

        // Context-aware authorization: MANAGER can only block rooms in their assigned hotel
        if ("MANAGER".equalsIgnoreCase(role)) {
            if (userHotelId == null) {
                throw new IllegalStateException("MANAGER must be assigned to a hotel");
            }
            if (!request.getHotelId().equals(userHotelId)) {
                throw new IllegalStateException("Forbidden: Cannot block room of another hotel");
            }
        }

        availabilityService.blockRoom(request);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Room blocked successfully for selected date range"
        ));
    }
    
    @PostMapping("/unblock")
    public ResponseEntity<?> unblockRoom(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader(value = "X-Hotel-Id", required = false) Long userHotelId,
            @Valid @RequestBody UnblockRoomRequest request
    ) {
        authorize(role);
        
        // Context-aware authorization: MANAGER can only unblock rooms in their assigned hotel
        if ("MANAGER".equalsIgnoreCase(role)) {
            if (userHotelId == null) {
                throw new IllegalStateException("MANAGER must be assigned to a hotel");
            }
            if (!request.getHotelId().equals(userHotelId)) {
                throw new IllegalStateException("Forbidden: Cannot unblock room of another hotel");
            }
        }
        
        availabilityService.unblockRoom(request);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Room unblocked successfully for selected date range"
        ));
    }

    private void authorize(String role) {
        if (!role.equalsIgnoreCase("ADMIN")
                && !role.equalsIgnoreCase("MANAGER")) {
            throw new IllegalStateException("Only ADMIN or MANAGER allowed");
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<?> searchAvailability(
            @RequestParam Long hotelId,
            @RequestParam LocalDate checkIn,
            @RequestParam LocalDate checkOut
    ) {

        var response = availabilityService.searchAvailability(
                hotelId,
                checkIn,
                checkOut
        );

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Availability search completed successfully",
                "data", response
        ));
    }

}
