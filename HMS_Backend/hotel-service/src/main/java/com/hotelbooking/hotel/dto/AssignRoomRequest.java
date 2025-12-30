package com.hotelbooking.hotel.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignRoomRequest {
    @NotBlank
    private String bookingId;
}