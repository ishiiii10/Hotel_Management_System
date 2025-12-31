package com.hotelbooking.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GuestDto {

    @NotBlank
    private String fullName;

    @Positive
    private int age;

    @NotBlank
    private String idType;

    @NotBlank
    private String idNumber;
}