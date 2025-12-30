package com.hotelbooking.hotel.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.hotelbooking.hotel.domain.RoomHold;

public interface RoomHoldRepository
        extends JpaRepository<RoomHold, Long> {

    @Query("""
        SELECT COALESCE(SUM(h.rooms), 0)
        FROM RoomHold h
        WHERE h.hotelId = :hotelId
          AND h.categoryId = :categoryId
          AND h.released = false
          AND h.expiresAt > :now
          AND h.checkInDate < :checkOut
          AND h.checkOutDate > :checkIn
    """)
    int countActiveHolds(
            Long hotelId,
            Long categoryId,
            LocalDate checkIn,
            LocalDate checkOut,
            LocalDateTime now
    );

    List<RoomHold> findByExpiresAtBeforeAndReleasedFalse(LocalDateTime now);
    Optional<RoomHold> findByHoldId(String holdId);
}