package com.hotelbooking.hotel.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotelbooking.hotel.domain.City;
import com.hotelbooking.hotel.domain.Hotel;
import com.hotelbooking.hotel.domain.HotelStatus;
import com.hotelbooking.hotel.domain.Hotel_Category;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    List<Hotel> findByCity(City city);

    List<Hotel> findByCityIgnoreCaseAndStatus(City city, HotelStatus status);

	Optional<Hotel> findByHotelCategoryAndStatus(Hotel_Category category, HotelStatus active);
    
    
    
}