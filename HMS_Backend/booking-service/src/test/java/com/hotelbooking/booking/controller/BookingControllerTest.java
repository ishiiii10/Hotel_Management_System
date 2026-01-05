package com.hotelbooking.booking.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.hotelbooking.booking.dto.request.CancelBookingRequest;
import com.hotelbooking.booking.dto.request.CheckInRequest;
import com.hotelbooking.booking.dto.request.CheckOutRequest;
import com.hotelbooking.booking.dto.request.CreateBookingRequest;
import com.hotelbooking.booking.dto.request.WalkInBookingRequest;
import com.hotelbooking.booking.dto.response.AvailabilityResponse;
import com.hotelbooking.booking.dto.response.BookingResponse;
import com.hotelbooking.booking.enums.BookingStatus;
import com.hotelbooking.booking.exception.AccessDeniedException;
import com.hotelbooking.booking.exception.BookingNotFoundException;
import com.hotelbooking.booking.exception.ValidationException;
import com.hotelbooking.booking.service.BookingService;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingController bookingController;

    private CreateBookingRequest createBookingRequest;
    private BookingResponse bookingResponse;
    private AvailabilityResponse availabilityResponse;

    @BeforeEach
    void setUp() {
        createBookingRequest = new CreateBookingRequest();
        createBookingRequest.setHotelId(1L);
        createBookingRequest.setRoomId(1L);
        createBookingRequest.setCheckInDate(LocalDate.now().plusDays(1));
        createBookingRequest.setCheckOutDate(LocalDate.now().plusDays(3));
        createBookingRequest.setNumberOfGuests(2);

        bookingResponse = BookingResponse.builder()
                .id(1L)
                .userId(1L)
                .hotelId(1L)
                .roomId(1L)
                .status(BookingStatus.CREATED)
                .totalAmount(BigDecimal.valueOf(2000))
                .build();

        availabilityResponse = new AvailabilityResponse(1L, 10L, 5L, Arrays.asList());
    }

    @Test
    void testCheckAvailability_Success() {
        when(bookingService.checkAvailability(1L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3)))
                .thenReturn(availabilityResponse);

        ResponseEntity<?> response = bookingController.checkAvailability(1L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(true, body.get("success"));
    }

    @Test
    void testCreateBooking_Success() {
        when(bookingService.createBooking(any(CreateBookingRequest.class), anyLong(), any(), any(), any(), any()))
                .thenReturn(bookingResponse);

        ResponseEntity<?> response = bookingController.createBooking(1L, "GUEST", "test@example.com", "testuser", createBookingRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(true, body.get("success"));
    }

    @Test
    void testCreateBooking_InvalidRole() {
        assertThrows(AccessDeniedException.class, () -> {
            bookingController.createBooking(1L, "RECEPTIONIST", "test@example.com", "testuser", createBookingRequest);
        });
    }

    @Test
    void testCreateWalkInBooking_Success() {
        WalkInBookingRequest walkInRequest = new WalkInBookingRequest();
        walkInRequest.setHotelId(1L);
        walkInRequest.setRoomId(1L);
        walkInRequest.setCheckInDate(LocalDate.now().plusDays(1));
        walkInRequest.setCheckOutDate(LocalDate.now().plusDays(3));
        walkInRequest.setGuestName("John Doe");
        walkInRequest.setGuestEmail("john@example.com");
        walkInRequest.setGuestPhone("1234567890");
        walkInRequest.setNumberOfGuests(2);

        when(bookingService.createWalkInBooking(any(WalkInBookingRequest.class), anyLong(), any()))
                .thenReturn(bookingResponse);

        ResponseEntity<?> response = bookingController.createWalkInBooking(1L, "RECEPTIONIST", walkInRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testCreateWalkInBooking_InvalidRole() {
        WalkInBookingRequest walkInRequest = new WalkInBookingRequest();
        assertThrows(AccessDeniedException.class, () -> {
            bookingController.createWalkInBooking(1L, "GUEST", walkInRequest);
        });
    }

    @Test
    void testGetBookingById_Success() {
        when(bookingService.getBookingById(1L)).thenReturn(bookingResponse);

        ResponseEntity<?> response = bookingController.getBookingById(1L, "GUEST", 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetBookingById_AccessDenied() {
        BookingResponse otherUserBooking = BookingResponse.builder()
                .id(1L)
                .userId(2L)
                .build();

        when(bookingService.getBookingById(1L)).thenReturn(otherUserBooking);

        assertThrows(AccessDeniedException.class, () -> {
            bookingController.getBookingById(1L, "GUEST", 1L);
        });
    }

    @Test
    void testGetMyBookings_Success() {
        List<BookingResponse> bookings = Arrays.asList(bookingResponse);
        when(bookingService.getMyBookings(1L)).thenReturn(bookings);

        ResponseEntity<?> response = bookingController.getMyBookings(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetBookingsByHotel_Success() {
        List<BookingResponse> bookings = Arrays.asList(bookingResponse);
        when(bookingService.getBookingsByHotel(1L)).thenReturn(bookings);

        ResponseEntity<?> response = bookingController.getBookingsByHotel("MANAGER", 1L, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetBookingsByHotel_InvalidRole() {
        assertThrows(AccessDeniedException.class, () -> {
            bookingController.getBookingsByHotel("GUEST", 1L, 1L);
        });
    }

    @Test
    void testGetBookingsByHotel_WrongHotel() {
        assertThrows(AccessDeniedException.class, () -> {
            bookingController.getBookingsByHotel("MANAGER", 1L, 2L);
        });
    }

    @Test
    void testGetAllBookings_Success() {
        List<BookingResponse> bookings = Arrays.asList(bookingResponse);
        when(bookingService.getAllBookings()).thenReturn(bookings);

        ResponseEntity<?> response = bookingController.getAllBookings("ADMIN");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetAllBookings_InvalidRole() {
        assertThrows(AccessDeniedException.class, () -> {
            bookingController.getAllBookings("GUEST");
        });
    }

    @Test
    void testConfirmBooking_Success() {
        BookingResponse confirmedBooking = BookingResponse.builder()
                .id(1L)
                .status(BookingStatus.CONFIRMED)
                .build();

        when(bookingService.confirmBooking(1L)).thenReturn(confirmedBooking);

        ResponseEntity<?> response = bookingController.confirmBooking("ADMIN", 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testConfirmBooking_InvalidRole() {
        assertThrows(AccessDeniedException.class, () -> {
            bookingController.confirmBooking("GUEST", 1L);
        });
    }

    @Test
    void testCancelBooking_Success() {
        CancelBookingRequest cancelRequest = new CancelBookingRequest();
        cancelRequest.setReason("Change of plans");

        BookingResponse cancelledBooking = BookingResponse.builder()
                .id(1L)
                .status(BookingStatus.CANCELLED)
                .build();

        when(bookingService.cancelBooking(anyLong(), anyLong(), any(), any())).thenReturn(cancelledBooking);

        ResponseEntity<?> response = bookingController.cancelBooking(1L, "GUEST", 1L, cancelRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testCheckIn_Success() {
        CheckInRequest checkInRequest = new CheckInRequest();
        BookingResponse checkedInBooking = BookingResponse.builder()
                .id(1L)
                .status(BookingStatus.CHECKED_IN)
                .build();

        when(bookingService.checkIn(anyLong(), anyLong(), any())).thenReturn(checkedInBooking);

        ResponseEntity<?> response = bookingController.checkIn("RECEPTIONIST", 1L, 1L, checkInRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testCheckIn_InvalidRole() {
        assertThrows(AccessDeniedException.class, () -> {
            bookingController.checkIn("GUEST", 1L, 1L, new CheckInRequest());
        });
    }

    @Test
    void testCheckOut_Success() {
        CheckOutRequest checkOutRequest = new CheckOutRequest();
        BookingResponse checkedOutBooking = BookingResponse.builder()
                .id(1L)
                .status(BookingStatus.CHECKED_OUT)
                .build();

        when(bookingService.checkOut(anyLong(), anyLong(), any())).thenReturn(checkedOutBooking);

        ResponseEntity<?> response = bookingController.checkOut("RECEPTIONIST", 1L, 1L, checkOutRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testCheckOut_InvalidRole() {
        assertThrows(AccessDeniedException.class, () -> {
            bookingController.checkOut("GUEST", 1L, 1L, new CheckOutRequest());
        });
    }

    @Test
    void testGetTodayCheckIns_Success() {
        List<BookingResponse> bookings = Arrays.asList(bookingResponse);
        when(bookingService.getTodayCheckIns(1L)).thenReturn(bookings);

        ResponseEntity<?> response = bookingController.getTodayCheckIns("RECEPTIONIST", 1L, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetTodayCheckIns_InvalidRole() {
        assertThrows(AccessDeniedException.class, () -> {
            bookingController.getTodayCheckIns("GUEST", 1L, 1L);
        });
    }

    @Test
    void testGetTodayCheckOuts_Success() {
        List<BookingResponse> bookings = Arrays.asList(bookingResponse);
        when(bookingService.getTodayCheckOuts(1L)).thenReturn(bookings);

        ResponseEntity<?> response = bookingController.getTodayCheckOuts("RECEPTIONIST", 1L, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetTodayCheckOuts_InvalidRole() {
        assertThrows(AccessDeniedException.class, () -> {
            bookingController.getTodayCheckOuts("GUEST", 1L, 1L);
        });
    }

    @Test
    void testGetBookingsByHotel_MissingHotelId() {
        assertThrows(ValidationException.class, () -> {
            bookingController.getBookingsByHotel("MANAGER", null, 1L);
        });
    }

    @Test
    void testGetTodayCheckIns_MissingHotelId() {
        assertThrows(ValidationException.class, () -> {
            bookingController.getTodayCheckIns("MANAGER", null, 1L);
        });
    }

    @Test
    void testGetTodayCheckOuts_MissingHotelId() {
        assertThrows(ValidationException.class, () -> {
            bookingController.getTodayCheckOuts("MANAGER", null, 1L);
        });
    }

    @Test
    void testCheckIn_WithNullRequest() {
        BookingResponse checkedInBooking = BookingResponse.builder()
                .id(1L)
                .status(BookingStatus.CHECKED_IN)
                .build();

        when(bookingService.checkIn(anyLong(), anyLong(), any())).thenReturn(checkedInBooking);

        ResponseEntity<?> response = bookingController.checkIn("RECEPTIONIST", 1L, 1L, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testCheckOut_WithNullRequest() {
        BookingResponse checkedOutBooking = BookingResponse.builder()
                .id(1L)
                .status(BookingStatus.CHECKED_OUT)
                .build();

        when(bookingService.checkOut(anyLong(), anyLong(), any())).thenReturn(checkedOutBooking);

        ResponseEntity<?> response = bookingController.checkOut("RECEPTIONIST", 1L, 1L, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}

