package com.hotelbooking.hotel.exception;

public class ValidationException extends HotelException {
    
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

