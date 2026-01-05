package com.hotelbooking.booking.exception;

import com.hotelbooking.booking.enums.BookingStatus;

public class InvalidBookingStatusException extends BookingException {
    
    public InvalidBookingStatusException(String message) {
        super(message != null ? message : "Invalid booking status");
    }
    
    public InvalidBookingStatusException(BookingStatus currentStatus, String operation) {
        super("Cannot " + operation + " booking with status: " + currentStatus);
    }
    
    public InvalidBookingStatusException() {
        super("Invalid booking status");
    }
}

