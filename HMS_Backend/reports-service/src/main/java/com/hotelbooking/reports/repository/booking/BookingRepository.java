package com.hotelbooking.reports.repository.booking;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hotelbooking.reports.domain.booking.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'CONFIRMED' AND (:hotelId IS NULL OR b.hotelId = :hotelId)")
    Long countTotalBookings(@Param("hotelId") Long hotelId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'CONFIRMED' " +
           "AND DATE(b.createdAt) = :today AND (:hotelId IS NULL OR b.hotelId = :hotelId)")
    Long countTodayBookings(@Param("today") LocalDate today, @Param("hotelId") Long hotelId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.checkInDate = :today " +
           "AND b.status IN ('CONFIRMED', 'CHECKED_IN') AND (:hotelId IS NULL OR b.hotelId = :hotelId)")
    Long countCheckInsToday(@Param("today") LocalDate today, @Param("hotelId") Long hotelId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.checkOutDate = :today " +
           "AND b.status IN ('CHECKED_IN', 'CHECKED_OUT') AND (:hotelId IS NULL OR b.hotelId = :hotelId)")
    Long countCheckOutsToday(@Param("today") LocalDate today, @Param("hotelId") Long hotelId);

    @Query("SELECT HOUR(b.createdAt) as hour, COUNT(b) as count " +
           "FROM Booking b WHERE b.status = 'CONFIRMED' " +
           "AND DATE(b.createdAt) = :today AND (:hotelId IS NULL OR b.hotelId = :hotelId) " +
           "GROUP BY HOUR(b.createdAt)")
    List<Object[]> findTodayBookingsByHour(@Param("today") LocalDate today, @Param("hotelId") Long hotelId);

    @Query("SELECT HOUR(b.createdAt) as hour, b.bookingSource, COUNT(b) as count " +
           "FROM Booking b WHERE b.status = 'CONFIRMED' " +
           "AND DATE(b.createdAt) = :today AND (:hotelId IS NULL OR b.hotelId = :hotelId) " +
           "GROUP BY HOUR(b.createdAt), b.bookingSource")
    List<Object[]> findTodayBookingsBySourceAndHour(@Param("today") LocalDate today, @Param("hotelId") Long hotelId);
}

