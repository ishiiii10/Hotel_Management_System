package com.hotelbooking.booking.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AvailabilityResponse {
    private Long hotelId;
    private Long totalRooms;
    private Long availableRooms;
    private List<AvailableRoom> availableRoomsList;
}

