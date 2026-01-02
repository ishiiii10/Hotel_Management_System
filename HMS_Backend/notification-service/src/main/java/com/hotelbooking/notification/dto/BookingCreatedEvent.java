package com.hotelbooking.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreatedEvent {
    private Long bookingId;
    private Long userId;
    private Long hotelId;
    private Long roomId;
    private String checkInDate;
    private String checkOutDate;
    private Double amount;
}

