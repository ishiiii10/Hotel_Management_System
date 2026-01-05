package com.hotelbooking.booking.exception;

public class RoomNotAvailableException extends BookingException {
    
    public RoomNotAvailableException(String message) {
        super(message != null ? message : "Room is not available");
    }
    
    public RoomNotAvailableException(Long roomId) {
        super("Room is not available: " + roomId);
    }
    
    public RoomNotAvailableException() {
        super("Room is not available");
    }
}

