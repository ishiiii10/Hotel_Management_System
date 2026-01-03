package com.hotelbooking.booking.event;

import java.io.Serializable;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingCancelledEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long bookingId;
    private Long userId;
    private Long hotelId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String cancellationReason;
    private String guestEmail;
    private String guestName;
}

