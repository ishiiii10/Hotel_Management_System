package com.hotelbooking.hotel.dto.response;


import java.time.LocalDateTime;

import com.hotelbooking.hotel.enums.City;
import com.hotelbooking.hotel.enums.HotelStatus;
import com.hotelbooking.hotel.enums.Hotel_Category;
import com.hotelbooking.hotel.enums.State;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HotelDetailResponse {

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
    private HotelStatus status;
    private Integer totalRooms;
    private Integer availableRooms;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}