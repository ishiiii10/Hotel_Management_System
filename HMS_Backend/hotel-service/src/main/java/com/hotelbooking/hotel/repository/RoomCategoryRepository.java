package com.hotelbooking.hotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotelbooking.hotel.domain.RoomCategory;

public interface RoomCategoryRepository extends JpaRepository<RoomCategory, Long> {
}
