package com.hotelbooking.hotel.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRoomCategoryRequest {

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal basePrice;

    @Min(1)
    private int maxOccupancy;

    private boolean active;
}