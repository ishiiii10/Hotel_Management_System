package com.hotelbooking.booking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotelbooking.booking.domain.Booking;

public interface BookingRepository
        extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingCode(String bookingCode);

    List<Booking> findByPrimaryGuestUserId(Long userId);

    List<Booking> findByHotelId(Long hotelId);
}