package com.hotelbooking.reports.repository.hotel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hotelbooking.reports.domain.hotel.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("SELECT COUNT(r) FROM Room r WHERE r.hotelId = :hotelId " +
           "AND r.isActive = true AND r.status = 'AVAILABLE'")
    Long countAvailableRooms(@Param("hotelId") Long hotelId);
}

