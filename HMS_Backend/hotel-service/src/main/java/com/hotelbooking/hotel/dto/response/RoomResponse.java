package com.hotelbooking.hotel.dto.response;


import java.io.Serializable;
import java.math.BigDecimal;

import com.hotelbooking.hotel.enums.RoomCategory;
import com.hotelbooking.hotel.enums.RoomStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoomResponse implements Serializable {

    private static final long serialVersionUID = 1L;

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
