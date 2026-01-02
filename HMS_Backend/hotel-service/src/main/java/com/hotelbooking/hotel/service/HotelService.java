package com.hotelbooking.hotel.service;

import java.util.List;

import com.hotelbooking.hotel.domain.City;
import com.hotelbooking.hotel.domain.Hotel_Category;
import com.hotelbooking.hotel.dto.CreateHotelRequest;
import com.hotelbooking.hotel.dto.HotelDetailResponse;
import com.hotelbooking.hotel.dto.HotelSearchResponse;

public interface HotelService {

    Long createHotel(CreateHotelRequest request);

    Long updateHotel(Long hotelId, CreateHotelRequest request);

    HotelDetailResponse getHotelById(Long hotelId);

    List<HotelSearchResponse> searchHotelsByCity(City city);
    
    List<HotelSearchResponse> searchHotelsByCategory(Hotel_Category category);

    List<HotelDetailResponse> getAllHotels();
}