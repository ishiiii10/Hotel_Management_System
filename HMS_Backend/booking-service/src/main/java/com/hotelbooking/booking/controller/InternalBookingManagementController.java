package com.hotelbooking.booking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.hotelbooking.booking.service.BookingService;
import com.hotelbooking.booking.domain.BookingStatus;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal/reservations")
@RequiredArgsConstructor
public class InternalBookingManagementController {
    private final BookingService bookingService;

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam BookingStatus status
    ) {
        bookingService.updateStatus(id, status);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/booked-room")
    public ResponseEntity<Object> getRoomsBookedByDate(@RequestParam String date) {
        return ResponseEntity.ok(bookingService.getRoomsBookedByDate(date));
    }

    @GetMapping("/start-tomorrow")
    public ResponseEntity<Object> getBookingsStartingTomorrow() {
        return ResponseEntity.ok(bookingService.getBookingsStartingTomorrow());
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<Object> getBookingSummary(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingSummary(id));
    }
}

