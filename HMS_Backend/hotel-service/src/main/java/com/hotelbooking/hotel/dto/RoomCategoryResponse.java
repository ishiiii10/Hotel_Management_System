package com.hotelbooking.hotel.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoomCategoryResponse {

    private Long categoryId;
    private Long hotelId;
    private String name;
    private BigDecimal basePrice;
    private int maxOccupancy;
    private boolean active;
}