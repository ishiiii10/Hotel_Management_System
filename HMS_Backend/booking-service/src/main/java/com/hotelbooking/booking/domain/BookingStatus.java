package com.hotelbooking.booking.domain;

public enum BookingStatus {
    CREATED,     // record created, not yet confirmed
    CONFIRMED,   // hold consumed successfully
    CHECKED_IN,  // guest checked in
    CHECKED_OUT, // guest checked out
    CANCELLED,   // cancelled by guest or system
    NO_SHOW      // guest did not show up
}