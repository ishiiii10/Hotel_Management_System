package com.hotelbooking.hotel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotelbooking.hotel.domain.PhysicalRoom;

public interface PhysicalRoomRepository
        extends JpaRepository<PhysicalRoom, Long> {

    List<PhysicalRoom> findByHotelId(Long hotelId);
}