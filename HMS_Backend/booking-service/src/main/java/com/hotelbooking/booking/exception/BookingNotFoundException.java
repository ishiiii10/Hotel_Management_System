package com.hotelbooking.booking.exception;

public class BookingNotFoundException extends BookingException {
    
    public BookingNotFoundException(String message) {
        super(message != null ? message : "Booking not found");
    }
    
    public BookingNotFoundException(Long bookingId) {
        super("Booking not found with ID: " + bookingId);
    }
    
    public BookingNotFoundException() {
        super("Booking not found");
    }
}

