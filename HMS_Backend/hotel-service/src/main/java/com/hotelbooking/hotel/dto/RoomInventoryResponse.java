package com.hotelbooking.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoomInventoryResponse {

    private Long hotelId;
    private Long categoryId;
    private int totalRooms;
    private int outOfService;
    private int availableRooms;
}