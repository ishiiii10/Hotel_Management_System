package com.hotelbooking.booking.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelbooking.booking.domain.Booking;
import com.hotelbooking.booking.dto.request.CancelBookingRequest;
import com.hotelbooking.booking.dto.request.CheckInRequest;
import com.hotelbooking.booking.dto.request.CheckOutRequest;
import com.hotelbooking.booking.dto.request.CreateBookingRequest;
import com.hotelbooking.booking.dto.request.WalkInBookingRequest;
import com.hotelbooking.booking.dto.response.AvailableRoom;
import com.hotelbooking.booking.dto.response.AvailabilityResponse;
import com.hotelbooking.booking.dto.response.BookingResponse;
import com.hotelbooking.booking.dto.response.HotelDetailResponse;
import com.hotelbooking.booking.dto.response.RoomResponse;
import com.hotelbooking.booking.enums.BookingStatus;
import com.hotelbooking.booking.exception.BookingNotFoundException;
import com.hotelbooking.booking.exception.InvalidBookingStatusException;
import com.hotelbooking.booking.exception.RoomNotAvailableException;
import com.hotelbooking.booking.exception.ValidationException;
import com.hotelbooking.booking.exception.AccessDeniedException;
import com.hotelbooking.booking.feign.HotelServiceClient;
import com.hotelbooking.booking.repository.BookingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final HotelServiceClient hotelServiceClient;
    private final KafkaEventPublisher kafkaEventPublisher;
    private final CacheManager cacheManager;

    /**
     * Check room availability for a hotel and date range.
     * Step 3a: Calls Hotel Service to get all rooms
     * Step 3b: Queries own DB for overlapping bookings
     * Step 3c: Filters available rooms
     */
    @Transactional(readOnly = true)
    public AvailabilityResponse checkAvailability(Long hotelId, LocalDate checkIn, LocalDate checkOut) {
        // Validate dates
        if (checkIn == null || checkOut == null) {
            throw new ValidationException("Check-in and check-out dates are required");
        }
        if (!checkIn.isBefore(checkOut)) {
            throw new ValidationException("Check-in must be before check-out");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new ValidationException("Check-in date cannot be in the past");
        }

        // Step 3a: Get all rooms from Hotel Service
        List<RoomResponse> allRooms = hotelServiceClient.getRoomsByHotel(hotelId);
        
        // Step 3b: Get booked room IDs (overlapping dates)
        List<Long> bookedRoomIds = bookingRepository.findBookedRoomIds(hotelId, checkIn, checkOut);
        
        // Step 3c: Filter available rooms
        List<AvailableRoom> availableRooms = allRooms.stream()
                .filter(room -> room.getIsActive() != null && room.getIsActive())
                .filter(room -> {
                    String status = room.getStatus();
                    return status != null && "AVAILABLE".equalsIgnoreCase(status);
                })
                .filter(room -> !bookedRoomIds.contains(room.getId()))
                .map(room -> new AvailableRoom(
                        room.getId(),
                        room.getRoomNumber(),
                        room.getRoomType(),
                        room.getPricePerNight(),
                        room.getMaxOccupancy(),
                        room.getAmenities(),
                        room.getDescription()
                ))
                .collect(Collectors.toList());

        return new AvailabilityResponse(
                hotelId,
                (long) allRooms.size(),
                (long) availableRooms.size(),
                availableRooms
        );
    }

    /**
     * Create walk-in booking for receptionist.
     * Creates booking with WALK_IN source and guest information.
     */
    public BookingResponse createWalkInBooking(WalkInBookingRequest request, Long receptionistUserId, String role) {
        CreateBookingRequest createRequest = new CreateBookingRequest();
        createRequest.setHotelId(request.getHotelId());
        createRequest.setRoomId(request.getRoomId());
        createRequest.setCheckInDate(request.getCheckInDate());
        createRequest.setCheckOutDate(request.getCheckOutDate());
        createRequest.setNumberOfGuests(request.getNumberOfGuests());
        createRequest.setSpecialRequests(request.getSpecialRequests());
        
        // Use receptionist's userId, but store guest info separately
        return createBooking(createRequest, receptionistUserId, request.getGuestName(), 
                           request.getGuestEmail(), request.getGuestPhone(), role);
    }

    /**
     * Create a new booking.
     * Step 5a: Double-check availability (prevent race condition)
     * Step 5b: Create booking with status PENDING
     * Step 5c: Publish Kafka event
     */
    public BookingResponse createBooking(CreateBookingRequest request, Long userId, String guestName, 
                                        String guestEmail, String guestPhone, String role) {
        // Validate hotel exists and is active
        HotelDetailResponse hotel = hotelServiceClient.getHotelById(request.getHotelId());
        String hotelStatus = hotel.getStatus();
        if (hotelStatus == null || !"ACTIVE".equalsIgnoreCase(hotelStatus)) {
            throw new ValidationException("Hotel is not active");
        }

        // Get room details
        RoomResponse room = hotelServiceClient.getRoomById(request.getRoomId());
        if (!room.getHotelId().equals(request.getHotelId())) {
            throw new ValidationException("Room does not belong to the specified hotel");
        }
        if (room.getIsActive() == null || !room.getIsActive()) {
            throw new RoomNotAvailableException("Room is not active");
        }
        String roomStatus = room.getStatus();
        if (roomStatus == null || !"AVAILABLE".equalsIgnoreCase(roomStatus)) {
            throw new RoomNotAvailableException("Room is not available");
        }

        // Step 5a: Double-check availability (prevent race condition)
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                request.getHotelId(),
                request.getRoomId(),
                request.getCheckInDate(),
                request.getCheckOutDate()
        );
        if (!overlappingBookings.isEmpty()) {
            throw new RoomNotAvailableException("Room is not available for the selected dates");
        }

        // Calculate total amount
        long nights = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        BigDecimal totalAmount = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        // Step 5b: Create booking with status CREATED
        // Payment service will confirm it and change status to CONFIRMED
        // Determine booking source: WALK_IN if MANAGER/ADMIN/RECEPTIONIST, PUBLIC if GUEST
        com.hotelbooking.booking.enums.BookingSource bookingSource = 
            ("MANAGER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role) || "RECEPTIONIST".equalsIgnoreCase(role)) 
                ? com.hotelbooking.booking.enums.BookingSource.WALK_IN 
                : com.hotelbooking.booking.enums.BookingSource.PUBLIC;
        
        Booking booking = Booking.builder()
                .userId(userId)
                .hotelId(request.getHotelId())
                .roomId(request.getRoomId())
                .roomNumber(room.getRoomNumber())
                .roomType(room.getRoomType())
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .totalAmount(totalAmount)
                .status(BookingStatus.CREATED)
                .bookingSource(bookingSource)
                .guestName(guestName)
                .guestEmail(guestEmail)
                .guestPhone(guestPhone)
                .numberOfGuests(request.getNumberOfGuests())
                .specialRequests(request.getSpecialRequests())
                .build();

        booking = bookingRepository.save(booking);

        // Step 5c: Publish BookingCreatedEvent
        com.hotelbooking.booking.event.BookingCreatedEvent createdEvent = 
            com.hotelbooking.booking.event.BookingCreatedEvent.builder()
                .bookingId(booking.getId())
                .userId(booking.getUserId())
                .hotelId(booking.getHotelId())
                .roomId(booking.getRoomId())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .amount(booking.getTotalAmount().doubleValue())
                .guestEmail(booking.getGuestEmail())
                .guestName(booking.getGuestName())
                .bookingSource(bookingSource.name())
                .build();
        kafkaEventPublisher.publishBookingCreated(createdEvent);

        // Evict caches
        evictBookingCaches(booking.getId(), booking.getUserId(), booking.getHotelId());

        return toResponse(booking);
    }

    /**
     * Confirm booking (called by Payment Service after successful payment)
     */
    public BookingResponse confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (booking.getStatus() != BookingStatus.CREATED) {
            throw new InvalidBookingStatusException(booking.getStatus(), "confirm");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        booking = bookingRepository.save(booking);

        // Publish BookingConfirmedEvent
        com.hotelbooking.booking.event.BookingConfirmedEvent confirmedEvent = 
            com.hotelbooking.booking.event.BookingConfirmedEvent.builder()
                .bookingId(booking.getId())
                .userId(booking.getUserId())
                .hotelId(booking.getHotelId())
                .roomId(booking.getRoomId())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .amount(booking.getTotalAmount().doubleValue())
                .guestEmail(booking.getGuestEmail())
                .guestName(booking.getGuestName())
                .build();
        kafkaEventPublisher.publishBookingConfirmed(confirmedEvent);

        // Evict caches
        evictBookingCaches(booking.getId(), booking.getUserId(), booking.getHotelId());

        return toResponse(booking);
    }

    /**
     * Get booking by ID
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "bookings", key = "#bookingId", unless = "#result == null")
    public BookingResponse getBookingById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
        return toResponse(booking);
    }

    /**
     * Get all bookings for current user
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "userBookings", key = "#userId")
    public List<BookingResponse> getMyBookings(Long userId) {
        return bookingRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all bookings for a specific hotel (staff only)
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "hotelBookings", key = "#hotelId")
    public List<BookingResponse> getBookingsByHotel(Long hotelId) {
        return bookingRepository.findByHotelId(hotelId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all bookings (admin only)
     */
    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cancel a booking
     */
    public BookingResponse cancelBooking(Long bookingId, Long userId, String role, CancelBookingRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        // Check ownership (GUEST can only cancel their own bookings, ADMIN can cancel any)
        if (!"ADMIN".equalsIgnoreCase(role) && !booking.getUserId().equals(userId)) {
            throw new AccessDeniedException("You can only cancel your own bookings");
        }

        // Check if booking can be cancelled
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new InvalidBookingStatusException(booking.getStatus(), "cancel");
        }
        if (booking.getStatus() == BookingStatus.CHECKED_IN || booking.getStatus() == BookingStatus.CHECKED_OUT) {
            throw new InvalidBookingStatusException(booking.getStatus(), "cancel");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(request.getReason());
        booking.setCancelledAt(java.time.LocalDateTime.now());

        booking = bookingRepository.save(booking);

        // Publish BookingCancelledEvent
        com.hotelbooking.booking.event.BookingCancelledEvent cancelledEvent = 
            com.hotelbooking.booking.event.BookingCancelledEvent.builder()
                .bookingId(booking.getId())
                .userId(booking.getUserId())
                .hotelId(booking.getHotelId())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .cancellationReason(booking.getCancellationReason())
                .guestEmail(booking.getGuestEmail())
                .guestName(booking.getGuestName())
                .build();
        kafkaEventPublisher.publishBookingCancelled(cancelledEvent);

        // Evict caches
        evictBookingCaches(booking.getId(), booking.getUserId(), booking.getHotelId());

        return toResponse(booking);
    }

    /**
     * Check-in guest
     */
    public BookingResponse checkIn(Long bookingId, Long hotelId, CheckInRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        // Verify hotel ownership
        if (!booking.getHotelId().equals(hotelId)) {
            throw new AccessDeniedException("Booking does not belong to this hotel");
        }

        // Check if booking can be checked in
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new InvalidBookingStatusException(booking.getStatus(), "check in");
        }

        LocalDateTime checkInTimestamp = java.time.LocalDateTime.now();
        booking.setStatus(BookingStatus.CHECKED_IN);
        booking.setCheckedInAt(checkInTimestamp);

        booking = bookingRepository.save(booking);

        // Publish GuestCheckedInEvent
        com.hotelbooking.booking.event.GuestCheckedInEvent checkedInEvent = 
            com.hotelbooking.booking.event.GuestCheckedInEvent.builder()
                .bookingId(booking.getId())
                .userId(booking.getUserId())
                .hotelId(booking.getHotelId())
                .checkInDate(booking.getCheckInDate())
                .actualCheckInTimestamp(checkInTimestamp)
                .guestEmail(booking.getGuestEmail())
                .guestName(booking.getGuestName())
                .build();
        kafkaEventPublisher.publishGuestCheckedIn(checkedInEvent);

        // Evict caches
        evictBookingCaches(booking.getId(), booking.getUserId(), booking.getHotelId());

        return toResponse(booking);
    }

    /**
     * Check-out guest
     */
    public BookingResponse checkOut(Long bookingId, Long hotelId, CheckOutRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        // Verify hotel ownership
        if (!booking.getHotelId().equals(hotelId)) {
            throw new AccessDeniedException("Booking does not belong to this hotel");
        }

        // Check if booking can be checked out
        if (booking.getStatus() != BookingStatus.CHECKED_IN) {
            throw new InvalidBookingStatusException(booking.getStatus(), "check out");
        }

        LocalDateTime checkoutTimestamp = java.time.LocalDateTime.now();
        booking.setStatus(BookingStatus.CHECKED_OUT);
        booking.setCheckedOutAt(checkoutTimestamp);

        booking = bookingRepository.save(booking);

        // Publish CheckoutCompletedEvent with all required fields
        com.hotelbooking.booking.event.CheckoutCompletedEvent checkoutEvent = 
            com.hotelbooking.booking.event.CheckoutCompletedEvent.builder()
                .bookingId(booking.getId())
                .userId(booking.getUserId())
                .hotelId(booking.getHotelId())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .actualCheckoutTimestamp(checkoutTimestamp)
                .guestEmail(booking.getGuestEmail())
                .guestName(booking.getGuestName())
                .build();
        kafkaEventPublisher.publishCheckoutCompleted(checkoutEvent);

        // Evict caches
        evictBookingCaches(booking.getId(), booking.getUserId(), booking.getHotelId());

        return toResponse(booking);
    }

    /**
     * Get today's check-ins for a hotel
     */
    @Transactional(readOnly = true)
    public List<BookingResponse> getTodayCheckIns(Long hotelId) {
        return bookingRepository.findTodayCheckIns(hotelId, LocalDate.now())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get today's check-outs for a hotel
     */
    @Transactional(readOnly = true)
    public List<BookingResponse> getTodayCheckOuts(Long hotelId) {
        return bookingRepository.findTodayCheckOuts(hotelId, LocalDate.now())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private void evictBookingCaches(Long bookingId, Long userId, Long hotelId) {
        // Evict individual booking
        var bookingCache = cacheManager.getCache("bookings");
        if (bookingCache != null) {
            bookingCache.evict(bookingId);
        }
        
        // Evict user bookings list
        var userBookingsCache = cacheManager.getCache("userBookings");
        if (userBookingsCache != null) {
            userBookingsCache.evict(userId);
        }
        
        // Evict hotel bookings list
        var hotelBookingsCache = cacheManager.getCache("hotelBookings");
        if (hotelBookingsCache != null) {
            hotelBookingsCache.evict(hotelId);
        }
    }

    private BookingResponse toResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUserId())
                .hotelId(booking.getHotelId())
                .roomId(booking.getRoomId())
                .roomNumber(booking.getRoomNumber())
                .roomType(booking.getRoomType())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus())
                .guestName(booking.getGuestName())
                .guestEmail(booking.getGuestEmail())
                .guestPhone(booking.getGuestPhone())
                .numberOfGuests(booking.getNumberOfGuests())
                .numberOfNights(booking.getNumberOfNights())
                .specialRequests(booking.getSpecialRequests())
                .cancellationReason(booking.getCancellationReason())
                .cancelledAt(booking.getCancelledAt())
                .checkedInAt(booking.getCheckedInAt())
                .checkedOutAt(booking.getCheckedOutAt())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .bookingSource(booking.getBookingSource() != null ? booking.getBookingSource().name() : null)
                .build();
    }
}

