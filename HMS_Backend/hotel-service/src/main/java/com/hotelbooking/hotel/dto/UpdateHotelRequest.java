package com.hotelbooking.hotel.dto;



import com.hotelbooking.hotel.domain.City;
import com.hotelbooking.hotel.domain.Hotel_Category;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateHotelRequest {

	@NotBlank
    private String name;

    @NotNull
    private City city;

    @NotBlank
    private String address;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Pattern(
        regexp = "^[+]?[0-9]{10,15}$",
        message = "Phone number must contain 10 to 15 digits and may start with +"
    )
    private String phoneNumber;

    @NotNull
    private Hotel_Category category;
}