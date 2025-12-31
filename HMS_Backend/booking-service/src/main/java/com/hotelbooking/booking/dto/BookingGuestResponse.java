package com.hotelbooking.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BookingGuestResponse {

    private final String fullName;
    private final int age;
    private final String idType;
    private final String idNumber;
}