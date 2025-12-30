package com.hotelbooking.hotel.dto;

import java.math.BigDecimal;
import java.util.List;

import com.hotelbooking.hotel.domain.City;
import com.hotelbooking.hotel.domain.Hotel_Category;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PublicHotelSearchResponse {

    private Long hotelId;
    private String name;
    private City city;
    private Hotel_Category hotelCategory;
    private List<AvailableCategory> availableCategories;

    @Getter
    @AllArgsConstructor
    public static class AvailableCategory {
        private Long categoryId;
        private String categoryName;
        private int availableRooms;
        private BigDecimal pricePerNight;
    }
}