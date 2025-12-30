package com.hotelbooking.hotel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotelbooking.hotel.domain.City;
import com.hotelbooking.hotel.domain.Hotel;
import com.hotelbooking.hotel.domain.HotelStatus;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    List<Hotel> findByCity(City city);

    List<Hotel> findByStatus(HotelStatus status);
    
    List<Hotel> findByCityAndStatus(City city, HotelStatus status);
}