package com.hotelbooking.hotel.service;

import java.time.LocalDate;

import com.hotelbooking.hotel.dto.request.BlockRoomRequest;
import com.hotelbooking.hotel.dto.request.UnblockRoomRequest;
import com.hotelbooking.hotel.dto.response.AvailabilitySearchResponse;

public interface RoomAvailabilityService {

    void blockRoom(BlockRoomRequest request);
    void unblockRoom(UnblockRoomRequest request);
    
    AvailabilitySearchResponse searchAvailability(
            Long hotelId,
            LocalDate checkIn,
            LocalDate checkOut
    );
}