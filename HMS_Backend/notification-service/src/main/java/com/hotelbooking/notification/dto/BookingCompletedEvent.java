package com.hotelbooking.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingCompletedEvent {
    private Long bookingId;
    private Long userId;
    private Long hotelId;
}

