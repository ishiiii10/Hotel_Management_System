package com.hotelbooking.hotel.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AvailabilityResponse {

    private Long hotelId;
    private Long categoryId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int availableRooms;
}