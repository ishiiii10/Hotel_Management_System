package com.hotelbooking.hotel.dto;



import com.hotelbooking.hotel.domain.City;
import com.hotelbooking.hotel.domain.HotelStatus;
import com.hotelbooking.hotel.domain.Hotel_Category;

import com.hotelbooking.hotel.domain.State;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HotelSearchResponse {

    private Long id;
    private String name;
    private Hotel_Category category;
    private String description;
    private String address;
    private City city;
    private State state;
    private String country;
    private String pincode;
    private String contactNumber;
    private String email;
    private Integer starRating;
    private String amenities;
    private String imageUrl;
    private HotelStatus status;
    private Integer totalRooms;
    private Integer availableRooms;
}