package com.hotelbooking.booking.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hotelbooking.booking.domain.Booking;
import com.hotelbooking.booking.enums.BookingSource;
import com.hotelbooking.booking.enums.BookingStatus;

@ExtendWith(MockitoExtension.class)
class BookingRepositoryTest {

    @Mock
    private BookingRepository bookingRepository;

    private Booking testBooking;

    @BeforeEach
    void setUp() {
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
    }

    @Test
    void testFindByUserId() {
        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingRepository.findByUserId(1L)).thenReturn(bookings);

        List<Booking> result = bookingRepository.findByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getUserId());
    }

    @Test
    void testFindByHotelId() {
        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingRepository.findByHotelId(1L)).thenReturn(bookings);

        List<Booking> result = bookingRepository.findByHotelId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getHotelId());
    }

    @Test
    void testFindById() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        Optional<Booking> result = bookingRepository.findById(1L);

        assertNotNull(result);
        assertEquals(true, result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void testFindByIdAndUserId() {
        when(bookingRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testBooking));

        Optional<Booking> result = bookingRepository.findByIdAndUserId(1L, 1L);

        assertNotNull(result);
        assertEquals(true, result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals(1L, result.get().getUserId());
    }

    @Test
    void testFindOverlappingBookings() {
        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingRepository.findOverlappingBookings(anyLong(), anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(bookings);

        List<Booking> result = bookingRepository.findOverlappingBookings(1L, 1L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testFindBookedRoomIds() {
        List<Long> bookedRoomIds = Arrays.asList(1L, 2L);
        when(bookingRepository.findBookedRoomIds(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(bookedRoomIds);

        List<Long> result = bookingRepository.findBookedRoomIds(1L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testFindTodayCheckIns() {
        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingRepository.findTodayCheckIns(anyLong(), any(LocalDate.class))).thenReturn(bookings);

        List<Booking> result = bookingRepository.findTodayCheckIns(1L, LocalDate.now());

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testFindTodayCheckOuts() {
        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingRepository.findTodayCheckOuts(anyLong(), any(LocalDate.class))).thenReturn(bookings);

        List<Booking> result = bookingRepository.findTodayCheckOuts(1L, LocalDate.now());

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}

