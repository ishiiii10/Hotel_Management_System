package com.hotelbooking.hotel.service;

import java.time.LocalDate;
import java.util.List;

import com.hotelbooking.hotel.dto.request.CreateHotelRequest;
import com.hotelbooking.hotel.dto.response.HotelDetailResponse;
import com.hotelbooking.hotel.dto.response.HotelSearchResponse;
import com.hotelbooking.hotel.enums.City;
import com.hotelbooking.hotel.enums.Hotel_Category;

public interface HotelService {

    Long createHotel(CreateHotelRequest request);

    Long updateHotel(Long hotelId, CreateHotelRequest request);

    HotelDetailResponse getHotelById(Long hotelId);
    
    List<HotelSearchResponse> searchHotels(
            City city,
            LocalDate checkIn,
            LocalDate checkOut,
            Hotel_Category category
    );

    List<HotelSearchResponse> searchHotelsByCity(City city);
    
    List<HotelSearchResponse> searchHotelsByCategory(Hotel_Category category);

    List<HotelDetailResponse> getAllHotels();
}