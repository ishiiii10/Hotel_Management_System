package com.hotelbooking.booking.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hotelbooking.booking.dto.response.BookingResponse;
import com.hotelbooking.booking.enums.BookingStatus;
import com.hotelbooking.booking.service.BookingService;

@ExtendWith(MockitoExtension.class)
class InternalBookingControllerTest {

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private InternalBookingController internalBookingController;

    private BookingResponse bookingResponse;

    @BeforeEach
    void setUp() {
        bookingResponse = BookingResponse.builder()
                .id(1L)
                .userId(1L)
                .hotelId(1L)
                .roomId(1L)
                .status(BookingStatus.CREATED)
                .totalAmount(BigDecimal.valueOf(2000))
                .checkInDate(LocalDate.now().plusDays(1))
                .checkOutDate(LocalDate.now().plusDays(3))
                .build();
    }

    @Test
    void testGetBookingById_Success() {
        when(bookingService.getBookingById(1L)).thenReturn(bookingResponse);

        BookingResponse response = internalBookingController.getBookingById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(BookingStatus.CREATED, response.getStatus());
    }

    @Test
    void testConfirmBooking_Success() {
        BookingResponse confirmedBooking = BookingResponse.builder()
                .id(1L)
                .status(BookingStatus.CONFIRMED)
                .build();

        when(bookingService.confirmBooking(1L)).thenReturn(confirmedBooking);

        BookingResponse response = internalBookingController.confirmBooking(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(BookingStatus.CONFIRMED, response.getStatus());
    }
}

