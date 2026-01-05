package com.hotelbooking.hotel.exception;

public class HotelNotFoundException extends HotelException {
    
    public HotelNotFoundException(String message) {
        super(message != null ? message : "Hotel not found");
    }
    
    public HotelNotFoundException(Long hotelId) {
        super("Hotel not found: " + hotelId);
    }
    
    public HotelNotFoundException() {
        super("Hotel not found");
    }
}

