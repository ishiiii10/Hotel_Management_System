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

    @PostMapping("/{id}/check-in")
    public ResponseEntity<Void> checkIn(@PathVariable Long id) {
        bookingService.checkIn(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/check-out")
    public ResponseEntity<Void> checkOut(@PathVariable Long id) {
        bookingService.checkOut(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/no-show")
    public ResponseEntity<Void> noShow(@PathVariable Long id) {
        bookingService.noShow(id);
        return ResponseEntity.noContent().build();
    }
}
