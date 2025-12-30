package com.hotelbooking.hotel.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRoomCategoryRequest {

    @NotBlank
    private String name;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal basePrice;

    @Min(1)
    private int maxOccupancy;
}