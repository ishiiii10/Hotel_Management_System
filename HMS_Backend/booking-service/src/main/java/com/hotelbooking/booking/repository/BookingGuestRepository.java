package com.hotelbooking.booking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotelbooking.booking.domain.BookingGuest;

public interface BookingGuestRepository
        extends JpaRepository<BookingGuest, Long> {

    List<BookingGuest> findByBookingId(Long bookingId);
}