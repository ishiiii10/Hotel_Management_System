package com.hotelbooking.booking.exception;

public class ValidationException extends BookingException {
    
    public ValidationException(String message) {
        super(message != null ? message : "Validation failed");
    }
    
    public ValidationException(String field, String reason) {
        super("Validation failed for field '" + field + "': " + reason);
    }
    
    public ValidationException() {
        super("Validation failed");
    }
}

