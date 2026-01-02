package com.hotelbooking.hotel.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AvailabilitySearchResponse{

    private Long hotelId;
    private int availableRooms;
    private List<Long> availableRoomIds;
}