package com.hotelbooking.hotel.exception;

public class RoomAlreadyExistsException extends HotelException {
    
    public RoomAlreadyExistsException(String message) {
        super(message != null ? message : "Room already exists");
    }
    
    public RoomAlreadyExistsException(String roomNumber, Long hotelId) {
        super("Room number " + roomNumber + " already exists for hotel: " + hotelId);
    }
    
    public RoomAlreadyExistsException() {
        super("Room already exists");
    }
}

