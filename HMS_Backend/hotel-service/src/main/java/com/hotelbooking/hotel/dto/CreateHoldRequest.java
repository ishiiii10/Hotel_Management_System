package com.hotelbooking.hotel.dto;

import java.time.LocalDate;



import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateHoldRequest {

    @NotNull
    private Long hotelId;

    @NotNull
    private Long categoryId;

    @NotNull
    private LocalDate checkInDate;

    @NotNull
    private LocalDate checkOutDate;

    @Min(1)
    private int rooms;
}