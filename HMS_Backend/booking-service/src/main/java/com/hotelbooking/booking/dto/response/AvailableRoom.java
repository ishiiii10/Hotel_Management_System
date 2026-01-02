package com.hotelbooking.booking.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AvailableRoom {
    private Long roomId;
    private String roomNumber;
    private String roomType;
    private BigDecimal pricePerNight;
    private Integer maxOccupancy;
    private String amenities;
    private String description;
}

