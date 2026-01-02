package com.hotelbooking.hotel.dto.request;



import com.hotelbooking.hotel.domain.City;
import com.hotelbooking.hotel.domain.State;
import com.hotelbooking.hotel.domain.HotelStatus;
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
    private Integer totalRooms;
    
    @NotNull
    private String imageUrl;

    
}