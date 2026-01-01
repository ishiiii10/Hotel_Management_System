package com.hotelbooking.booking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.hotelbooking.booking.service.BookingService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reservations/lifecycle")
@RequiredArgsConstructor
public class BookingLifecycleController {
    private final BookingService bookingService;

    @PostMapping("/{id}/confirm")
    public ResponseEntity<Void> confirm(@PathVariable Long id, @RequestHeader("X-User-Role") String role) {
        bookingService.confirm(id, role);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/check-in")
    public ResponseEntity<Void> checkIn(@PathVariable Long id, @RequestHeader("X-User-Role") String role) {
        bookingService.checkIn(id, role);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/check-out")
    public ResponseEntity<Void> checkOut(@PathVariable Long id, @RequestHeader("X-User-Role") String role) {
        bookingService.checkOut(id, role);
        return ResponseEntity.noContent().build();
    }
}
