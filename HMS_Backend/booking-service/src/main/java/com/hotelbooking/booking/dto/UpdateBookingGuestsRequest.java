package com.hotelbooking.booking.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateBookingGuestsRequest {

    @NotEmpty
    private List<GuestDto> guests;
}