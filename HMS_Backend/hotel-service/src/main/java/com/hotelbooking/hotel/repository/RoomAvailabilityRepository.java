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
    
    /**
     * Find room IDs that are available for ALL dates in the given range.
     * A room is available if:
     * 1. Room is active (isActive = true)
     * 2. Room status is AVAILABLE (not INACTIVE, MAINTENANCE, or OUT_OF_SERVICE)
     * 3. For ALL dates in the range, the room has availability status = AVAILABLE
     * 4. No dates in the range have status BLOCKED, RESERVED, or UNAVAILABLE
     * 
     * Invariants enforced:
     * - Availability is date-based (calculated per date)
     * - Room status affects availability (INACTIVE/MAINTENANCE/OUT_OF_SERVICE rooms excluded)
     * - Blocked rooms reduce availability immediately
     * - Bookings (RESERVED status) affect availability when overlapping
     */
    @Query("""
            SELECT DISTINCT r.id
            FROM Room r
            WHERE r.hotelId = :hotelId
            AND r.isActive = true
            AND r.status = 'AVAILABLE'
            AND r.id NOT IN (
                SELECT DISTINCT ra.roomId
                FROM RoomAvailability ra
                WHERE ra.hotelId = :hotelId
                AND ra.date BETWEEN :checkIn AND :checkOut
                AND ra.status IN ('BLOCKED', 'RESERVED', 'UNAVAILABLE')
            )
            AND NOT EXISTS (
                SELECT 1
                FROM RoomAvailability ra2
                WHERE ra2.roomId = r.id
                AND ra2.date BETWEEN :checkIn AND :checkOut
                AND ra2.status != 'AVAILABLE'
            )
            """)
    List<Long> findAvailableRoomIdsStrict(
            @Param("hotelId") Long hotelId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );
}