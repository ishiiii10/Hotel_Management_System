package com.hotelbooking.hotel.dto.response;


import java.math.BigDecimal;

import com.hotelbooking.hotel.domain.RoomStatus;
import com.hotelbooking.hotel.domain.RoomCategory;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoomResponse{

    private Long id;
    private Long hotelId;
    private String roomNumber;
    private RoomCategory roomType;
    private BigDecimal pricePerNight;
    private Integer maxOccupancy;
    private Integer floorNumber;
    private String bedType;
    private Integer roomSize;
    private String amenities;
    private String description;
    private RoomStatus status;
    private Boolean isActive;
}