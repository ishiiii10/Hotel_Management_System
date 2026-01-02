package com.hotelbooking.hotel.dto.request;



import com.hotelbooking.hotel.enums.City;

import com.hotelbooking.hotel.enums.HotelStatus;
import com.hotelbooking.hotel.enums.Hotel_Category;
import com.hotelbooking.hotel.enums.State;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateHotelRequest {

    @NotBlank
    private String name;
    
    @NotNull
    private Hotel_Category category;

    

    private String description;
    
    @NotNull
    private City city;
    private String address;
    private State state;
    private String country;
    private String pincode;

    private String contactNumber;
    private String email;

    private Integer starRating;
    private String amenities;

    @NotNull
    private HotelStatus status;

    
    @NotNull
    private String imageUrl;

    
}