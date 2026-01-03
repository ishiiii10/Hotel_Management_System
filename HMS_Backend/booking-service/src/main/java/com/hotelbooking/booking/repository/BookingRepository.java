package com.hotelbooking.booking.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hotelbooking.booking.domain.Booking;
import com.hotelbooking.booking.enums.BookingStatus;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);

    List<Booking> findByHotelId(Long hotelId);

    List<Booking> findByHotelIdAndStatus(Long hotelId, BookingStatus status);

    Optional<Booking> findByIdAndUserId(Long id, Long userId);

    /**
     * Find bookings with overlapping dates for a specific hotel and room.
     * Used to check availability - excludes CANCELLED bookings.
     */
    @Query("""
            SELECT b FROM Booking b
            WHERE b.hotelId = :hotelId
            AND b.roomId = :roomId
            AND b.status != 'CANCELLED'
            AND (
                (b.checkInDate <= :checkIn AND b.checkOutDate > :checkIn)
                OR (b.checkInDate < :checkOut AND b.checkOutDate >= :checkOut)
                OR (b.checkInDate >= :checkIn AND b.checkOutDate <= :checkOut)
            )
            """)
    List<Booking> findOverlappingBookings(
            @Param("hotelId") Long hotelId,
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );

    /**
     * Find all room IDs that are booked (have overlapping dates) for a hotel.
     */
    @Query("""
            SELECT DISTINCT b.roomId FROM Booking b
            WHERE b.hotelId = :hotelId
            AND b.status != 'CANCELLED'
            AND (
                (b.checkInDate <= :checkIn AND b.checkOutDate > :checkIn)
                OR (b.checkInDate < :checkOut AND b.checkOutDate >= :checkOut)
                OR (b.checkInDate >= :checkIn AND b.checkOutDate <= :checkOut)
            )
            """)
    List<Long> findBookedRoomIds(
            @Param("hotelId") Long hotelId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );

    /**
     * Find today's check-ins for a hotel.
     */
    @Query("""
            SELECT b FROM Booking b
            WHERE b.hotelId = :hotelId
            AND b.checkInDate = :date
            AND b.status IN ('CONFIRMED', 'CHECKED_IN')
            ORDER BY b.checkInDate ASC
            """)
    List<Booking> findTodayCheckIns(@Param("hotelId") Long hotelId, @Param("date") LocalDate date);

    /**
     * Find bookings that need check-in reminder (24 hours before check-in)
     */
    @Query("""
            SELECT b FROM Booking b
            WHERE b.status = 'CONFIRMED'
            AND b.checkInDate = :reminderDate
            """)
    List<Booking> findBookingsForCheckInReminder(@Param("reminderDate") LocalDate reminderDate);

    /**
     * Find today's check-outs for a hotel.
     */
    @Query("""
            SELECT b FROM Booking b
            WHERE b.hotelId = :hotelId
            AND b.checkOutDate = :date
            AND b.status IN ('CHECKED_IN', 'CHECKED_OUT')
            ORDER BY b.checkOutDate ASC
            """)
    List<Booking> findTodayCheckOuts(@Param("hotelId") Long hotelId, @Param("date") LocalDate date);
}

