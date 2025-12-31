package com.hotelbooking.booking.dto;

import java.time.LocalDate;

import com.hotelbooking.booking.domain.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BookingResponse {

    private String bookingCode;
    private Long hotelId;
    private Long categoryId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BookingStatus status;
}