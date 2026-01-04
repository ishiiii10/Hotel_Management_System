package com.hotelbooking.booking.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AvailableRoom implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long roomId;
    private String roomNumber;
    private String roomType;
    private BigDecimal pricePerNight;
    private Integer maxOccupancy;
    private String amenities;
    private String description;
}
