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
    private final com.hotelbooking.booking.kafka.BookingEventPublisher bookingEventPublisher;

    @Transactional
    public Booking createBooking(Long userId, CreateBookingRequest request) {
        if (bookingRepository.existsByHoldId(request.getHoldId())) {
            throw new IllegalStateException("Hold already consumed");
        }
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
        hotelServiceClient.releaseHold(request.getHoldId());
        saved.setStatus(BookingStatus.CONFIRMED);
        Booking confirmed = bookingRepository.save(saved);
        bookingEventPublisher.publish("booking.created", confirmed);
        return confirmed;
    }

    @Transactional
    public void confirm(Long bookingId, String role) {
        if (!"GUEST".equals(role)) throw new IllegalStateException("Only guest can confirm");
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found"));
        if (booking.getStatus() != BookingStatus.CREATED) throw new IllegalStateException("Booking not in CREATED");
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
        bookingEventPublisher.publish("booking.confirmed", booking);
    }

    @Transactional
    public void checkIn(Long bookingId, String role) {
        if (!"RECEPTIONIST".equals(role)) throw new IllegalStateException("Only receptionist can check-in");
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found"));
        if (booking.getStatus() != BookingStatus.CONFIRMED) throw new IllegalStateException("Booking not confirmed");
        booking.setStatus(BookingStatus.CHECKED_IN);
        bookingRepository.save(booking);
        bookingEventPublisher.publish("booking.checked_in", booking);
    }

    @Transactional
    public void checkOut(Long bookingId, String role) {
        if (!"RECEPTIONIST".equals(role)) throw new IllegalStateException("Only receptionist can check-out");
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found"));
        if (booking.getStatus() != BookingStatus.CHECKED_IN) throw new IllegalStateException("Booking not checked-in");
        booking.setStatus(BookingStatus.CHECKED_OUT);
        bookingRepository.save(booking);
        bookingEventPublisher.publish("booking.checked_out", booking);
    }

    @Transactional
    public void updateStatus(Long bookingId, BookingStatus status) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found"));
        booking.setStatus(status);
        bookingRepository.save(booking);
        bookingEventPublisher.publish("booking.status_updated", booking);
    }

    public Object getRoomsBookedByDate(String date) {
        // Dummy: return count or list
        return bookingRepository.findAll();
    }

    public Object getBookingsStartingTomorrow() {
        // Dummy: return bookings starting tomorrow
        return bookingRepository.findAll();
    }

    public Object getBookingSummary(Long id) {
        // Dummy: return booking summary
        return bookingRepository.findById(id).orElse(null);
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
        // ...existing logic...
        // After cancelling booking, publish event
        bookingEventPublisher.publish("booking.cancelled", booking);
    }
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
    
    @Transactional
    public void addWalkInGuests(
            Long bookingId,
            String role,
            Long staffHotelId,
            List<GuestDto> guests
    ) {
        if (!"RECEPTIONIST".equals(role)) {
            throw new IllegalStateException("Access denied");
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found"));

        if (!booking.getHotelId().equals(staffHotelId)) {
            throw new IllegalStateException("Access denied");
        }

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Guests can only be added to confirmed bookings");
        }

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