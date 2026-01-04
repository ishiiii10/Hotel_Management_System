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

    @Query("SELECT COUNT(b) FROM Booking b " +
           "WHERE b.status = 'CHECKED_IN' AND (:hotelId IS NULL OR b.hotelId = :hotelId)")
    Long countTotalCheckIns(@Param("hotelId") Long hotelId);

    @Query("SELECT COUNT(b) FROM Booking b " +
           "WHERE b.status = 'CHECKED_OUT' AND (:hotelId IS NULL OR b.hotelId = :hotelId)")
    Long countTotalCheckOuts(@Param("hotelId") Long hotelId);

    @Query("SELECT b.status, COUNT(b) as count " +
           "FROM Booking b WHERE (:hotelId IS NULL OR b.hotelId = :hotelId) " +
           "GROUP BY b.status")
    List<Object[]> findBookingStatusDistribution(@Param("hotelId") Long hotelId);

    @Query("SELECT DATE(b.createdAt) as date, COUNT(b) as count " +
           "FROM Booking b WHERE b.status = 'CONFIRMED' " +
           "AND b.createdAt >= :startDate AND b.createdAt < :endDate " +
           "AND (:hotelId IS NULL OR b.hotelId = :hotelId) " +
           "GROUP BY DATE(b.createdAt) " +
           "ORDER BY DATE(b.createdAt)")
    List<Object[]> findBookingTrend(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate,
                                     @Param("hotelId") Long hotelId);

    @Query("SELECT COUNT(b) FROM Booking b " +
           "WHERE (:hotelId IS NULL OR b.hotelId = :hotelId)")
    Long countAllBookings(@Param("hotelId") Long hotelId);

    @Query(value = "SELECT b.id, b.guest_name, b.guest_email, b.check_in_date, b.check_out_date, b.total_amount, b.status " +
           "FROM bookings b " +
           "WHERE b.hotel_id = :hotelId " +
           "ORDER BY b.created_at DESC " +
           "LIMIT 10", nativeQuery = true)
    List<Object[]> findRecentBookingsByHotel(@Param("hotelId") Long hotelId);
}

