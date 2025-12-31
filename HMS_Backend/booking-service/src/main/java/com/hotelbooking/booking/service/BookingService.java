package com.hotelbooking.booking.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelbooking.booking.client.HotelServiceClient;
import com.hotelbooking.booking.domain.Booking;
import com.hotelbooking.booking.domain.BookingGuest;
import com.hotelbooking.booking.domain.BookingStatus;
import com.hotelbooking.booking.dto.CreateBookingRequest;
import com.hotelbooking.booking.dto.GuestDto;
import com.hotelbooking.booking.repository.BookingGuestRepository;
import com.hotelbooking.booking.repository.BookingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final HotelServiceClient hotelServiceClient;
    private final BookingGuestRepository bookingGuestRepository;

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
    
    public Booking getBookingById(Long bookingId, Long requesterId, String role) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found"));

        if ("GUEST".equals(role)
                && !booking.getPrimaryGuestUserId().equals(requesterId)) {
            throw new IllegalStateException("Access denied");
        }

        return booking;
    }
    
    public List<Booking> getBookingsForGuest(Long userId) {
        return bookingRepository.findByPrimaryGuestUserId(userId);
    }
    
    public List<Booking> getBookingsForHotel(
            Long hotelId,
            Long requesterHotelId,
            String role
    ) {
        if ("MANAGER".equals(role) && !hotelId.equals(requesterHotelId)) {
            throw new IllegalStateException("Access denied");
        }

        return bookingRepository.findByHotelId(hotelId);
    }
    
    public List<BookingGuest> getGuestsForBooking(
            Long bookingId,
            Long requesterId,
            String role
    ) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found"));

        if ("GUEST".equals(role)
                && !booking.getPrimaryGuestUserId().equals(requesterId)) {
            throw new IllegalStateException("Access denied");
        }

        return bookingGuestRepository.findByBookingId(bookingId);
    }
    
    @Transactional
    public void cancelBooking(
            Long bookingId,
            Long requesterId,
            String role,
            Long staffHotelId
    ) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found"));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking already cancelled");
        }

        switch (role) {

            case "GUEST" -> {
                if (!booking.getPrimaryGuestUserId().equals(requesterId)) {
                    throw new IllegalStateException("Access denied");
                }
            }

            case "MANAGER" -> {
                if (!booking.getHotelId().equals(staffHotelId)) {
                    throw new IllegalStateException("Access denied");
                }
            }

            case "ADMIN" -> {
                // allowed
            }

            default -> throw new IllegalStateException("Access denied");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());

        bookingRepository.save(booking);

        // Phase-2: publish BOOKING_CANCELLED event
    }
    
    @Transactional
    public void updateGuests(
            Long bookingId,
            Long requesterId,
            String role,
            List<GuestDto> guests
    ) {
        if (!"GUEST".equals(role)) {
            throw new IllegalStateException("Access denied");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found"));

        if (!booking.getPrimaryGuestUserId().equals(requesterId)) {
            throw new IllegalStateException("Access denied");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot modify cancelled booking");
        }

        // delete old guests
        bookingGuestRepository.deleteAll(
                bookingGuestRepository.findByBookingId(bookingId)
        );

        // add new guests
        guests.forEach(g ->
                bookingGuestRepository.save(
                        BookingGuest.builder()
                                .bookingId(bookingId)
                                .fullName(g.getFullName())
                                .age(g.getAge())
                                .idType(g.getIdType())
                                .idNumber(g.getIdNumber())
                                .build()
                )
        );
    }
}