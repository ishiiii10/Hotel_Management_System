package com.hotelbooking.booking.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponse {
    private Long id;
    private Long hotelId;
    private String roomNumber;
    private String roomType; // RoomCategory as String
    private BigDecimal pricePerNight;
    private Integer maxOccupancy;
    private Integer floorNumber;
    private String bedType;
    private Integer roomSize;
    private String amenities;
    private String description;
    private String status; // RoomStatus as String
    private Boolean isActive;
}

