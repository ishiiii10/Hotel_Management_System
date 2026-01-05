package com.hotelbooking.hotel.exception;

public class AccessDeniedException extends HotelException {
    
    public AccessDeniedException(String message) {
        super(message != null ? message : "Access denied");
    }
    
    public AccessDeniedException() {
        super("Access denied");
    }
}

