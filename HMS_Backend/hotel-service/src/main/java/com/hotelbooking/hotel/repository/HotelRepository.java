package com.hotelbooking.hotel.repository;

import java.util.List;



import org.springframework.data.jpa.repository.JpaRepository;

import com.hotelbooking.hotel.domain.Hotel;
import com.hotelbooking.hotel.enums.City;
import com.hotelbooking.hotel.enums.HotelStatus;
import com.hotelbooking.hotel.enums.Hotel_Category;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    List<Hotel> findByCity(City city);
    
    List<Hotel> findByCityAndStatus(City city, HotelStatus status);

    List<Hotel> findByCityAndCategoryAndStatus(
            City city,
            Hotel_Category category,
            HotelStatus status
    );


	List<Hotel> findByCategoryAndStatus(Hotel_Category category, HotelStatus active);
    
    
    
}