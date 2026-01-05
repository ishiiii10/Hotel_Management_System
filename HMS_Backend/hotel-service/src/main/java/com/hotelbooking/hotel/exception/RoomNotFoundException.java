package com.hotelbooking.hotel.exception;

public class RoomNotFoundException extends HotelException {
    
    public RoomNotFoundException(String message) {
        super(message != null ? message : "Room not found");
    }
    
    public RoomNotFoundException(Long roomId) {
        super("Room not found: " + roomId);
    }
    
    public RoomNotFoundException() {
        super("Room not found");
    }
}

