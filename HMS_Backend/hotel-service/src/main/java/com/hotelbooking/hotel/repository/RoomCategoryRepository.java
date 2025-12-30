package com.hotelbooking.hotel.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotelbooking.hotel.domain.RoomCategory;

public interface RoomCategoryRepository
        extends JpaRepository<RoomCategory, Long> {

    List<RoomCategory> findByHotelId(Long hotelId);

    Optional<RoomCategory> findByIdAndHotelId(Long id, Long hotelId);
}