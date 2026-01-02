package com.hotelbooking.hotel.controller;



import java.time.LocalDate;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @Valid @RequestBody BlockRoomRequest request
    ) {

        if (!role.equalsIgnoreCase("ADMIN")
                && !role.equalsIgnoreCase("MANAGER")) {
            throw new IllegalStateException("Only ADMIN or MANAGER can block rooms");
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
            @Valid @RequestBody UnblockRoomRequest request
    ) {
        authorize(role);
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
