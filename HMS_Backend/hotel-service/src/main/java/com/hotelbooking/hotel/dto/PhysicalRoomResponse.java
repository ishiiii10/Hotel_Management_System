package com.hotelbooking.hotel.dto;

import com.hotelbooking.hotel.domain.RoomState;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PhysicalRoomResponse {

    private Long roomId;
    private Long hotelId;
    private Long categoryId;
    private String roomNumber;
    private RoomState state;
    private Integer floor;
}