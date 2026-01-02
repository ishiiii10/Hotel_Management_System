package com.hotelbooking.hotel.repository;



import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotelbooking.hotel.domain.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {
    long countByHotelId(Long hotelId);

    boolean existsByHotelIdAndRoomNumber(Long hotelId, String roomNumber);
    
    Optional<Room> findByIdAndIsActiveTrue(Long id);

    List<Room> findByHotelId(Long hotelId);

    Optional<Room> findByIdAndHotelId(Long id, Long hotelId);
    
    long countByHotelIdAndIsActiveTrue(Long hotelId);
}