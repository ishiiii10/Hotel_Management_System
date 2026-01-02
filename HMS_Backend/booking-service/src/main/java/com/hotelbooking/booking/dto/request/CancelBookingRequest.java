package com.hotelbooking.booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelBookingRequest {
    @NotBlank(message = "Cancellation reason is required")
    private String reason;
}

