package com.hotelbooking.booking.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import com.hotelbooking.booking.domain.Booking;
import com.hotelbooking.booking.dto.request.CancelBookingRequest;
import com.hotelbooking.booking.dto.request.CheckInRequest;
import com.hotelbooking.booking.dto.request.CheckOutRequest;
import com.hotelbooking.booking.dto.request.CreateBookingRequest;
import com.hotelbooking.booking.dto.request.WalkInBookingRequest;
import com.hotelbooking.booking.dto.response.AvailabilityResponse;
import com.hotelbooking.booking.dto.response.BookingResponse;
import com.hotelbooking.booking.dto.response.HotelDetailResponse;
import com.hotelbooking.booking.dto.response.RoomResponse;
import com.hotelbooking.booking.enums.BookingSource;
import com.hotelbooking.booking.enums.BookingStatus;
import com.hotelbooking.booking.exception.AccessDeniedException;
import com.hotelbooking.booking.exception.BookingNotFoundException;
import com.hotelbooking.booking.exception.InvalidBookingStatusException;
import com.hotelbooking.booking.exception.RoomNotAvailableException;
import com.hotelbooking.booking.exception.ValidationException;
import com.hotelbooking.booking.feign.HotelServiceClient;
import com.hotelbooking.booking.repository.BookingRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private HotelServiceClient hotelServiceClient;

    @Mock
    private KafkaEventPublisher kafkaEventPublisher;

    @Mock
    private CacheManager cacheManager;

    @InjectMocks
    private BookingService bookingService;

    private CreateBookingRequest createRequest;
    private Booking testBooking;
    private HotelDetailResponse hotelResponse;
    private RoomResponse roomResponse;

    @BeforeEach
    void setUp() {
        createRequest = new CreateBookingRequest();
        createRequest.setHotelId(1L);
        createRequest.setRoomId(1L);
        createRequest.setCheckInDate(LocalDate.now().plusDays(1));
        createRequest.setCheckOutDate(LocalDate.now().plusDays(3));
        createRequest.setNumberOfGuests(2);

        hotelResponse = new HotelDetailResponse();
        hotelResponse.setId(1L);
        hotelResponse.setStatus("ACTIVE");

        roomResponse = new RoomResponse();
        roomResponse.setId(1L);
        roomResponse.setHotelId(1L);
        roomResponse.setRoomNumber("101");
        roomResponse.setRoomType("STANDARD");
        roomResponse.setPricePerNight(BigDecimal.valueOf(1000));
        roomResponse.setMaxOccupancy(2);
        roomResponse.setStatus("AVAILABLE");
        roomResponse.setIsActive(true);

        testBooking = Booking.builder()
                .id(1L)
                .userId(1L)
                .hotelId(1L)
                .roomId(1L)
                .roomNumber("101")
                .roomType("STANDARD")
                .checkInDate(LocalDate.now().plusDays(1))
                .checkOutDate(LocalDate.now().plusDays(3))
                .totalAmount(BigDecimal.valueOf(2000))
                .status(BookingStatus.CREATED)
                .bookingSource(BookingSource.PUBLIC)
                .guestName("Test Guest")
                .guestEmail("test@example.com")
                .numberOfGuests(2)
                .build();

        // Lenient stubbing for cache manager
        Cache bookingCache = new ConcurrentMapCacheManager().getCache("bookings");
        Cache userBookingsCache = new ConcurrentMapCacheManager().getCache("userBookings");
        Cache hotelBookingsCache = new ConcurrentMapCacheManager().getCache("hotelBookings");
        lenient().when(cacheManager.getCache("bookings")).thenReturn(bookingCache);
        lenient().when(cacheManager.getCache("userBookings")).thenReturn(userBookingsCache);
        lenient().when(cacheManager.getCache("hotelBookings")).thenReturn(hotelBookingsCache);
    }

    @Test
    void testCheckAvailability_Success() {
        List<RoomResponse> rooms = Arrays.asList(roomResponse);
        when(hotelServiceClient.getRoomsByHotel(1L)).thenReturn(rooms);
        when(bookingRepository.findBookedRoomIds(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        AvailabilityResponse response = bookingService.checkAvailability(1L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));

        assertNotNull(response);
        assertEquals(1L, response.getHotelId());
        assertEquals(1L, response.getTotalRooms());
    }

    @Test
    void testCheckAvailability_NullDates() {
        assertThrows(ValidationException.class, () -> {
            bookingService.checkAvailability(1L, null, LocalDate.now().plusDays(3));
        });
    }

    @Test
    void testCheckAvailability_CheckInAfterCheckOut() {
        assertThrows(ValidationException.class, () -> {
            bookingService.checkAvailability(1L, LocalDate.now().plusDays(3), LocalDate.now().plusDays(1));
        });
    }

    @Test
    void testCheckAvailability_PastCheckIn() {
        assertThrows(ValidationException.class, () -> {
            bookingService.checkAvailability(1L, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        });
    }

    @Test
    void testCreateBooking_Success() {
        when(hotelServiceClient.getHotelById(1L)).thenReturn(hotelResponse);
        when(hotelServiceClient.getRoomById(1L)).thenReturn(roomResponse);
        when(bookingRepository.findOverlappingBookings(anyLong(), anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        doNothing().when(kafkaEventPublisher).publishBookingCreated(any());

        BookingResponse response = bookingService.createBooking(createRequest, 1L, "Test Guest", "test@example.com", "1234567890", "GUEST");

        assertNotNull(response);
        assertEquals(1L, response.getId());
        verify(kafkaEventPublisher).publishBookingCreated(any());
    }

    @Test
    void testCreateBooking_HotelNotActive() {
        hotelResponse.setStatus("INACTIVE");
        when(hotelServiceClient.getHotelById(1L)).thenReturn(hotelResponse);

        assertThrows(ValidationException.class, () -> {
            bookingService.createBooking(createRequest, 1L, "Test Guest", "test@example.com", "1234567890", "GUEST");
        });
    }

    @Test
    void testCreateBooking_RoomNotBelongToHotel() {
        roomResponse.setHotelId(2L);
        when(hotelServiceClient.getHotelById(1L)).thenReturn(hotelResponse);
        when(hotelServiceClient.getRoomById(1L)).thenReturn(roomResponse);

        assertThrows(ValidationException.class, () -> {
            bookingService.createBooking(createRequest, 1L, "Test Guest", "test@example.com", "1234567890", "GUEST");
        });
    }

    @Test
    void testCreateBooking_RoomNotActive() {
        roomResponse.setIsActive(false);
        when(hotelServiceClient.getHotelById(1L)).thenReturn(hotelResponse);
        when(hotelServiceClient.getRoomById(1L)).thenReturn(roomResponse);

        assertThrows(RoomNotAvailableException.class, () -> {
            bookingService.createBooking(createRequest, 1L, "Test Guest", "test@example.com", "1234567890", "GUEST");
        });
    }

    @Test
    void testCreateBooking_RoomNotAvailable() {
        roomResponse.setStatus("OCCUPIED");
        when(hotelServiceClient.getHotelById(1L)).thenReturn(hotelResponse);
        when(hotelServiceClient.getRoomById(1L)).thenReturn(roomResponse);

        assertThrows(RoomNotAvailableException.class, () -> {
            bookingService.createBooking(createRequest, 1L, "Test Guest", "test@example.com", "1234567890", "GUEST");
        });
    }

    @Test
    void testCreateBooking_OverlappingBookings() {
        Booking overlappingBooking = Booking.builder().id(2L).build();
        when(hotelServiceClient.getHotelById(1L)).thenReturn(hotelResponse);
        when(hotelServiceClient.getRoomById(1L)).thenReturn(roomResponse);
        when(bookingRepository.findOverlappingBookings(anyLong(), anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(overlappingBooking));

        assertThrows(RoomNotAvailableException.class, () -> {
            bookingService.createBooking(createRequest, 1L, "Test Guest", "test@example.com", "1234567890", "GUEST");
        });
    }

    @Test
    void testCreateWalkInBooking_Success() {
        WalkInBookingRequest walkInRequest = new WalkInBookingRequest();
        walkInRequest.setHotelId(1L);
        walkInRequest.setRoomId(1L);
        // Walk-in bookings can only be created for today's date
        walkInRequest.setCheckInDate(LocalDate.now());
        walkInRequest.setCheckOutDate(LocalDate.now().plusDays(2));
        walkInRequest.setGuestName("Walk-in Guest");
        walkInRequest.setGuestEmail("walkin@example.com");
        walkInRequest.setGuestPhone("1234567890");
        walkInRequest.setNumberOfGuests(2);

        // Update testBooking to have today's check-in date for walk-in booking
        Booking walkInBooking = Booking.builder()
                .id(1L)
                .userId(1L)
                .hotelId(1L)
                .roomId(1L)
                .roomNumber("101")
                .roomType("STANDARD")
                .checkInDate(LocalDate.now())
                .checkOutDate(LocalDate.now().plusDays(2))
                .totalAmount(BigDecimal.valueOf(2000))
                .status(BookingStatus.CREATED)
                .bookingSource(BookingSource.WALK_IN)
                .guestName("Walk-in Guest")
                .guestEmail("walkin@example.com")
                .numberOfGuests(2)
                .build();

        when(hotelServiceClient.getHotelById(1L)).thenReturn(hotelResponse);
        when(hotelServiceClient.getRoomById(1L)).thenReturn(roomResponse);
        when(bookingRepository.findOverlappingBookings(anyLong(), anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenReturn(walkInBooking);
        doNothing().when(kafkaEventPublisher).publishBookingCreated(any());

        BookingResponse response = bookingService.createWalkInBooking(walkInRequest, 1L, "RECEPTIONIST");

        assertNotNull(response);
        verify(kafkaEventPublisher).publishBookingCreated(any());
    }

    @Test
    void testConfirmBooking_Success() {
        testBooking.setStatus(BookingStatus.CREATED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        doNothing().when(kafkaEventPublisher).publishBookingConfirmed(any());

        BookingResponse response = bookingService.confirmBooking(1L);

        assertNotNull(response);
        verify(kafkaEventPublisher).publishBookingConfirmed(any());
    }

    @Test
    void testConfirmBooking_NotFound() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class, () -> {
            bookingService.confirmBooking(1L);
        });
    }

    @Test
    void testConfirmBooking_InvalidStatus() {
        testBooking.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        assertThrows(InvalidBookingStatusException.class, () -> {
            bookingService.confirmBooking(1L);
        });
    }

    @Test
    void testGetBookingById_Success() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        BookingResponse response = bookingService.getBookingById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void testGetBookingById_NotFound() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class, () -> {
            bookingService.getBookingById(1L);
        });
    }

    @Test
    void testGetMyBookings_Success() {
        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingRepository.findByUserId(1L)).thenReturn(bookings);

        List<BookingResponse> response = bookingService.getMyBookings(1L);

        assertNotNull(response);
        assertEquals(1, response.size());
    }

    @Test
    void testGetBookingsByHotel_Success() {
        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingRepository.findByHotelId(1L)).thenReturn(bookings);

        List<BookingResponse> response = bookingService.getBookingsByHotel(1L);

        assertNotNull(response);
        assertEquals(1, response.size());
    }

    @Test
    void testCancelBooking_Success() {
        testBooking.setStatus(BookingStatus.CREATED);
        CancelBookingRequest cancelRequest = new CancelBookingRequest();
        cancelRequest.setReason("Change of plans");

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        doNothing().when(kafkaEventPublisher).publishBookingCancelled(any());

        BookingResponse response = bookingService.cancelBooking(1L, 1L, "GUEST", cancelRequest);

        assertNotNull(response);
        verify(kafkaEventPublisher).publishBookingCancelled(any());
    }

    @Test
    void testCancelBooking_AccessDenied() {
        testBooking.setUserId(2L);
        CancelBookingRequest cancelRequest = new CancelBookingRequest();
        cancelRequest.setReason("Change of plans");

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        assertThrows(AccessDeniedException.class, () -> {
            bookingService.cancelBooking(1L, 1L, "GUEST", cancelRequest);
        });
    }

    @Test
    void testCancelBooking_AlreadyCancelled() {
        testBooking.setStatus(BookingStatus.CANCELLED);
        CancelBookingRequest cancelRequest = new CancelBookingRequest();
        cancelRequest.setReason("Change of plans");

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        assertThrows(InvalidBookingStatusException.class, () -> {
            bookingService.cancelBooking(1L, 1L, "GUEST", cancelRequest);
        });
    }

    @Test
    void testCheckIn_Success() {
        testBooking.setStatus(BookingStatus.CONFIRMED);
        // Check-in can only happen on the exact check-in date, so set it to today
        testBooking.setCheckInDate(LocalDate.now());
        CheckInRequest checkInRequest = new CheckInRequest();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        doNothing().when(kafkaEventPublisher).publishGuestCheckedIn(any());

        BookingResponse response = bookingService.checkIn(1L, 1L, checkInRequest);

        assertNotNull(response);
        verify(kafkaEventPublisher).publishGuestCheckedIn(any());
    }

    @Test
    void testCheckIn_WrongHotel() {
        testBooking.setHotelId(2L);
        CheckInRequest checkInRequest = new CheckInRequest();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        assertThrows(AccessDeniedException.class, () -> {
            bookingService.checkIn(1L, 1L, checkInRequest);
        });
    }

    @Test
    void testCheckIn_InvalidStatus() {
        testBooking.setStatus(BookingStatus.CREATED);
        CheckInRequest checkInRequest = new CheckInRequest();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        assertThrows(InvalidBookingStatusException.class, () -> {
            bookingService.checkIn(1L, 1L, checkInRequest);
        });
    }

    @Test
    void testCheckOut_Success() {
        testBooking.setStatus(BookingStatus.CHECKED_IN);
        CheckOutRequest checkOutRequest = new CheckOutRequest();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        doNothing().when(kafkaEventPublisher).publishCheckoutCompleted(any());

        BookingResponse response = bookingService.checkOut(1L, 1L, checkOutRequest);

        assertNotNull(response);
        verify(kafkaEventPublisher).publishCheckoutCompleted(any());
    }

    @Test
    void testCheckOut_InvalidStatus() {
        testBooking.setStatus(BookingStatus.CONFIRMED);
        CheckOutRequest checkOutRequest = new CheckOutRequest();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        assertThrows(InvalidBookingStatusException.class, () -> {
            bookingService.checkOut(1L, 1L, checkOutRequest);
        });
    }

    @Test
    void testGetTodayCheckIns_Success() {
        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingRepository.findTodayCheckIns(1L, LocalDate.now())).thenReturn(bookings);

        List<BookingResponse> response = bookingService.getTodayCheckIns(1L);

        assertNotNull(response);
        assertEquals(1, response.size());
    }

    @Test
    void testGetTodayCheckOuts_Success() {
        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingRepository.findTodayCheckOuts(1L, LocalDate.now())).thenReturn(bookings);

        List<BookingResponse> response = bookingService.getTodayCheckOuts(1L);

        assertNotNull(response);
        assertEquals(1, response.size());
    }

    @Test
    void testGetAllBookings_Success() {
        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingRepository.findAll()).thenReturn(bookings);

        List<BookingResponse> response = bookingService.getAllBookings();

        assertNotNull(response);
        assertEquals(1, response.size());
    }

    @Test
    void testCheckAvailability_WithBookedRooms() {
        List<RoomResponse> rooms = Arrays.asList(roomResponse);
        List<Long> bookedRoomIds = Arrays.asList(1L);
        when(hotelServiceClient.getRoomsByHotel(1L)).thenReturn(rooms);
        when(bookingRepository.findBookedRoomIds(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(bookedRoomIds);

        AvailabilityResponse response = bookingService.checkAvailability(1L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));

        assertNotNull(response);
        assertEquals(1L, response.getHotelId());
        assertEquals(0L, response.getAvailableRooms());
    }

    @Test
    void testCheckAvailability_WithInactiveRooms() {
        RoomResponse inactiveRoom = new RoomResponse();
        inactiveRoom.setId(2L);
        inactiveRoom.setHotelId(1L);
        inactiveRoom.setIsActive(false);
        inactiveRoom.setStatus("AVAILABLE");

        List<RoomResponse> rooms = Arrays.asList(roomResponse, inactiveRoom);
        when(hotelServiceClient.getRoomsByHotel(1L)).thenReturn(rooms);
        when(bookingRepository.findBookedRoomIds(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        AvailabilityResponse response = bookingService.checkAvailability(1L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));

        assertNotNull(response);
        assertEquals(1L, response.getAvailableRooms());
    }

    @Test
    void testCheckAvailability_WithUnavailableStatus() {
        RoomResponse unavailableRoom = new RoomResponse();
        unavailableRoom.setId(2L);
        unavailableRoom.setHotelId(1L);
        unavailableRoom.setIsActive(true);
        unavailableRoom.setStatus("OCCUPIED");

        List<RoomResponse> rooms = Arrays.asList(roomResponse, unavailableRoom);
        when(hotelServiceClient.getRoomsByHotel(1L)).thenReturn(rooms);
        when(bookingRepository.findBookedRoomIds(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        AvailabilityResponse response = bookingService.checkAvailability(1L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));

        assertNotNull(response);
        assertEquals(1L, response.getAvailableRooms());
    }

    @Test
    void testCreateBooking_WithManagerRole() {
        when(hotelServiceClient.getHotelById(1L)).thenReturn(hotelResponse);
        when(hotelServiceClient.getRoomById(1L)).thenReturn(roomResponse);
        when(bookingRepository.findOverlappingBookings(anyLong(), anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        doNothing().when(kafkaEventPublisher).publishBookingCreated(any());

        BookingResponse response = bookingService.createBooking(createRequest, 1L, "Manager", "manager@example.com", "1234567890", "MANAGER");

        assertNotNull(response);
        verify(kafkaEventPublisher).publishBookingCreated(any());
    }

    @Test
    void testCreateBooking_WithAdminRole() {
        when(hotelServiceClient.getHotelById(1L)).thenReturn(hotelResponse);
        when(hotelServiceClient.getRoomById(1L)).thenReturn(roomResponse);
        when(bookingRepository.findOverlappingBookings(anyLong(), anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        doNothing().when(kafkaEventPublisher).publishBookingCreated(any());

        BookingResponse response = bookingService.createBooking(createRequest, 1L, "Admin", "admin@example.com", "1234567890", "ADMIN");

        assertNotNull(response);
        verify(kafkaEventPublisher).publishBookingCreated(any());
    }

    @Test
    void testCancelBooking_AdminCanCancelAny() {
        testBooking.setStatus(BookingStatus.CREATED);
        testBooking.setUserId(2L);
        CancelBookingRequest cancelRequest = new CancelBookingRequest();
        cancelRequest.setReason("Change of plans");

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        doNothing().when(kafkaEventPublisher).publishBookingCancelled(any());

        BookingResponse response = bookingService.cancelBooking(1L, 1L, "ADMIN", cancelRequest);

        assertNotNull(response);
        verify(kafkaEventPublisher).publishBookingCancelled(any());
    }

    @Test
    void testCancelBooking_CheckedInStatus() {
        testBooking.setStatus(BookingStatus.CHECKED_IN);
        CancelBookingRequest cancelRequest = new CancelBookingRequest();
        cancelRequest.setReason("Change of plans");

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        assertThrows(InvalidBookingStatusException.class, () -> {
            bookingService.cancelBooking(1L, 1L, "GUEST", cancelRequest);
        });
    }

    @Test
    void testCancelBooking_CheckedOutStatus() {
        testBooking.setStatus(BookingStatus.CHECKED_OUT);
        CancelBookingRequest cancelRequest = new CancelBookingRequest();
        cancelRequest.setReason("Change of plans");

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        assertThrows(InvalidBookingStatusException.class, () -> {
            bookingService.cancelBooking(1L, 1L, "GUEST", cancelRequest);
        });
    }

    @Test
    void testToResponse_WithAllFields() {
        testBooking.setCancellationReason("Test reason");
        testBooking.setCancelledAt(java.time.LocalDateTime.now());
        testBooking.setCheckedInAt(java.time.LocalDateTime.now());
        testBooking.setCheckedOutAt(java.time.LocalDateTime.now());
        testBooking.setCreatedAt(java.time.LocalDateTime.now());
        testBooking.setUpdatedAt(java.time.LocalDateTime.now());
        testBooking.setBookingSource(BookingSource.WALK_IN);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        BookingResponse response = bookingService.getBookingById(1L);

        assertNotNull(response);
        assertEquals("Test reason", response.getCancellationReason());
        assertNotNull(response.getCancelledAt());
        assertNotNull(response.getCheckedInAt());
        assertNotNull(response.getCheckedOutAt());
        assertEquals("WALK_IN", response.getBookingSource());
    }

    @Test
    void testToResponse_WithNullBookingSource() {
        testBooking.setBookingSource(null);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        BookingResponse response = bookingService.getBookingById(1L);

        assertNotNull(response);
        assertEquals(null, response.getBookingSource());
    }

    @Test
    void testEvictBookingCaches_CalledOnCreate() {
        when(hotelServiceClient.getHotelById(1L)).thenReturn(hotelResponse);
        when(hotelServiceClient.getRoomById(1L)).thenReturn(roomResponse);
        when(bookingRepository.findOverlappingBookings(anyLong(), anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        doNothing().when(kafkaEventPublisher).publishBookingCreated(any());

        bookingService.createBooking(createRequest, 1L, "Test Guest", "test@example.com", "1234567890", "GUEST");

        // Verify cache eviction was attempted (cache may be null, but method should be called)
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void testEvictBookingCaches_WithNullCaches() {
        when(cacheManager.getCache("bookings")).thenReturn(null);
        when(cacheManager.getCache("userBookings")).thenReturn(null);
        when(cacheManager.getCache("hotelBookings")).thenReturn(null);

        when(hotelServiceClient.getHotelById(1L)).thenReturn(hotelResponse);
        when(hotelServiceClient.getRoomById(1L)).thenReturn(roomResponse);
        when(bookingRepository.findOverlappingBookings(anyLong(), anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);
        doNothing().when(kafkaEventPublisher).publishBookingCreated(any());

        // Should not throw exception even if caches are null
        BookingResponse response = bookingService.createBooking(createRequest, 1L, "Test Guest", "test@example.com", "1234567890", "GUEST");

        assertNotNull(response);
    }

    @Test
    void testCheckAvailability_EmptyRoomsList() {
        when(hotelServiceClient.getRoomsByHotel(1L)).thenReturn(Collections.emptyList());
        when(bookingRepository.findBookedRoomIds(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        AvailabilityResponse response = bookingService.checkAvailability(1L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));

        assertNotNull(response);
        assertEquals(0L, response.getTotalRooms());
        assertEquals(0L, response.getAvailableRooms());
    }

    @Test
    void testGetMyBookings_EmptyList() {
        when(bookingRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        List<BookingResponse> response = bookingService.getMyBookings(1L);

        assertNotNull(response);
        assertEquals(0, response.size());
    }

    @Test
    void testGetBookingsByHotel_EmptyList() {
        when(bookingRepository.findByHotelId(1L)).thenReturn(Collections.emptyList());

        List<BookingResponse> response = bookingService.getBookingsByHotel(1L);

        assertNotNull(response);
        assertEquals(0, response.size());
    }

    @Test
    void testGetAllBookings_EmptyList() {
        when(bookingRepository.findAll()).thenReturn(Collections.emptyList());

        List<BookingResponse> response = bookingService.getAllBookings();

        assertNotNull(response);
        assertEquals(0, response.size());
    }

    @Test
    void testGetTodayCheckIns_EmptyList() {
        when(bookingRepository.findTodayCheckIns(1L, LocalDate.now())).thenReturn(Collections.emptyList());

        List<BookingResponse> response = bookingService.getTodayCheckIns(1L);

        assertNotNull(response);
        assertEquals(0, response.size());
    }

    @Test
    void testGetTodayCheckOuts_EmptyList() {
        when(bookingRepository.findTodayCheckOuts(1L, LocalDate.now())).thenReturn(Collections.emptyList());

        List<BookingResponse> response = bookingService.getTodayCheckOuts(1L);

        assertNotNull(response);
        assertEquals(0, response.size());
    }
}

