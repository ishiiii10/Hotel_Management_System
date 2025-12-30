package com.hotelbooking.hotel.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotelbooking.hotel.domain.RoomInventory;

public interface RoomInventoryRepository
        extends JpaRepository<RoomInventory, Long> {

    Optional<RoomInventory> findByHotelIdAndCategoryId(
            Long hotelId,
            Long categoryId
    );

    List<RoomInventory> findByHotelId(Long hotelId);
}