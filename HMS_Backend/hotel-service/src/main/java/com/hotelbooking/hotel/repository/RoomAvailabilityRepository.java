package com.hotelbooking.hotel.repository;



import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hotelbooking.hotel.domain.RoomAvailability;
import com.hotelbooking.hotel.enums.AvailabilityStatus;

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
    		SELECT DISTINCT ra.roomId
    		FROM RoomAvailability ra
    		WHERE ra.hotelId = :hotelId
    		AND ra.roomId NOT IN (
    		    SELECT r2.roomId
    		    FROM RoomAvailability r2
    		    WHERE r2.status <> 'AVAILABLE'
    		    AND r2.date BETWEEN :checkIn AND :checkOut
    		)
    		AND ra.date BETWEEN :checkIn AND :checkOut
    		""")
    		List<Long> findAvailableRoomIdsStrict(
    		        @Param("hotelId") Long hotelId,
    		        @Param("checkIn") LocalDate checkIn,
    		        @Param("checkOut") LocalDate checkOut
    		);
}