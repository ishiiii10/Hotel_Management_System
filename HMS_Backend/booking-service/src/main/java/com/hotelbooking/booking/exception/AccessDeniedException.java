package com.hotelbooking.booking.exception;

public class AccessDeniedException extends BookingException {
    
    public AccessDeniedException(String message) {
        super(message != null ? message : "Access denied");
    }
    
    public AccessDeniedException() {
        super("Access denied");
    }
}

