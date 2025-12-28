package com.hotelbooking.hotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotelbooking.hotel.domain.HotelBrand;

public interface HotelBrandRepository extends JpaRepository<HotelBrand, Long> {
    boolean existsByName(String name);
}
