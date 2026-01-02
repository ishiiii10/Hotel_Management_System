package com.hotelbooking.hotel.repository;



import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hotelbooking.hotel.domain.RoomAvailability;
import com.hotelbooking.hotel.domain.AvailabilityStatus;

public interface RoomAvailabilityRepository
        extends JpaRepository<RoomAvailability, Long> {

    List<RoomAvailability> findByHotelIdAndDateBetweenAndStatus(
            Long hotelId,
            LocalDate startDate,
            LocalDate endDate,
            AvailabilityStatus status
    );

    List<RoomAvailability> findByRoomIdAndDateBetween(
            Long roomId,
            LocalDate startDate,
            LocalDate endDate
    );

    boolean existsByRoomIdAndDate(
            Long roomId,
            LocalDate date
    );
    
    Optional<RoomAvailability> findByRoomIdAndDate(
            Long roomId,
            LocalDate date
    );
    
    @Query("""
            SELECT ra.roomId
            FROM RoomAvailability ra
            WHERE ra.hotelId = :hotelId
              AND ra.date >= :checkIn
              AND ra.date < :checkOut
            GROUP BY ra.roomId
            HAVING COUNT(CASE WHEN ra.status <> :available THEN 1 END) = 0
        """)
        List<Long> findAvailableRoomIds(
                @Param("hotelId") Long hotelId,
                @Param("checkIn") LocalDate checkIn,
                @Param("checkOut") LocalDate checkOut,
                @Param("available") AvailabilityStatus available
        );
}