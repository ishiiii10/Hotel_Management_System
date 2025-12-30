package com.hotelbooking.hotel.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hotelbooking.hotel.domain.RoomHold;
import com.hotelbooking.hotel.dto.CreateHoldRequest;
import com.hotelbooking.hotel.dto.CreateHoldResponse;
import com.hotelbooking.hotel.service.RoomHoldService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RoomHoldController {

    private final RoomHoldService holdService;

    /* ---------------- SYSTEM ---------------- */

    @PostMapping("/hotels/holds")
    public ResponseEntity<CreateHoldResponse> createHold(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody CreateHoldRequest request
    ) {
        requireSystem(role);

        RoomHold hold = holdService.createHold(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateHoldResponse(
                        hold.getHoldId(),
                        hold.getExpiresAt()
                ));
    }

    @PostMapping("/hotels/holds/{holdId}/release")
    public ResponseEntity<Void> releaseHold(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable String holdId
    ) {
        requireSystem(role);
        holdService.releaseHold(holdId);
        return ResponseEntity.noContent().build();
    }

    /* ---------------- Helpers ---------------- */

    private void requireSystem(String role) {
        if (!"SYSTEM".equals(role)) {
            throw new IllegalStateException("Only system can manage holds");
        }
    }
}