package com.hotelbooking.booking.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelbooking.booking.client.HotelServiceClient;
import com.hotelbooking.booking.domain.Booking;
import com.hotelbooking.booking.domain.BookingStatus;
import com.hotelbooking.booking.dto.CreateBookingRequest;
import com.hotelbooking.booking.repository.BookingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final HotelServiceClient hotelServiceClient;

    @Transactional
    public Booking createBooking(
            Long userId,
            CreateBookingRequest request
    ) {

        // 1. Create booking in CREATED state
        Booking booking = Booking.builder()
                .bookingCode(generateBookingCode())
                .hotelId(request.getHotelId())
                .categoryId(request.getCategoryId())
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .primaryGuestUserId(userId)
                .holdId(request.getHoldId())
                .status(BookingStatus.CREATED)
                .build();

        Booking saved = bookingRepository.save(booking);

        // 2. Release hold (CRITICAL STEP)
        hotelServiceClient.releaseHold(request.getHoldId());

        // 3. Confirm booking
        saved.setStatus(BookingStatus.CONFIRMED);
        return bookingRepository.save(saved);
    }

    private String generateBookingCode() {
        return "BOOK-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}