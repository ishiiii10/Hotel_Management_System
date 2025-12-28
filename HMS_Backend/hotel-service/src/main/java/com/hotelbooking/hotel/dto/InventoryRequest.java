package com.hotelbooking.hotel.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryRequest {

    @NotNull
    private Long roomCategoryId;

    @Min(1)
    private int totalRooms;

    private BigDecimal priceOverride;
}