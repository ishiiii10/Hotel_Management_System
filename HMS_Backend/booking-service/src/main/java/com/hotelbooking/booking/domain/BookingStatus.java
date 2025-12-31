package com.hotelbooking.booking.domain;

public enum BookingStatus {

    CREATED,     // record created, not yet confirmed
    CONFIRMED,   // hold consumed successfully
    CANCELLED    // cancelled by guest or system

    // Phase 2 (later)
    // CHECKED_IN
    // CHECKED_OUT
    // NO_SHOW
}