package com.hotelbooking.hotel.dto;



import com.hotelbooking.hotel.domain.City;
import com.hotelbooking.hotel.domain.HotelStatus;
import com.hotelbooking.hotel.domain.Hotel_Category;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HotelListResponse {

    private Long hotelId;
    private String name;
    private City city;
    private String email;
    private String phoneNumber;
    private Hotel_Category hotel_category;
    private HotelStatus status;
}