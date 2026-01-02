package com.hotelbooking.hotel.service;


import java.util.List;

import com.hotelbooking.hotel.dto.request.CreateRoomRequest;
import com.hotelbooking.hotel.dto.response.RoomResponse;
import com.hotelbooking.hotel.domain.Room;
import com.hotelbooking.hotel.domain.RoomStatus;

public interface RoomService {

    Long createRoom(CreateRoomRequest request);

    List<RoomResponse> getRoomsByHotel(Long hotelId);
    
    Long updateRoom(Long roomId, CreateRoomRequest request);

    void updateRoomStatus(Long roomId, RoomStatus status);

    void updateRoomActiveStatus(Long roomId, boolean isActive);
}