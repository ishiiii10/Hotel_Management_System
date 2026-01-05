package com.hotelbooking.booking.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.hotelbooking.booking.enums.BookingStatus;

class ExceptionClassesTest {

    @Test
    void testBookingNotFoundException_WithMessage() {
        BookingNotFoundException ex = new BookingNotFoundException("Booking not found");

        assertEquals("Booking not found", ex.getMessage());
    }

    @Test
    void testBookingNotFoundException_WithId() {
        BookingNotFoundException ex = new BookingNotFoundException(1L);

        assertNotNull(ex.getMessage());
        assertEquals("Booking not found with ID: 1", ex.getMessage());
    }

    @Test
    void testBookingNotFoundException_Default() {
        BookingNotFoundException ex = new BookingNotFoundException();

        assertNotNull(ex.getMessage());
        assertEquals("Booking not found", ex.getMessage());
    }

    @Test
    void testValidationException_WithMessage() {
        ValidationException ex = new ValidationException("Validation failed");

        assertEquals("Validation failed", ex.getMessage());
    }

    @Test
    void testValidationException_WithField() {
        ValidationException ex = new ValidationException("field", "Field is required");

        assertNotNull(ex.getMessage());
    }

    @Test
    void testValidationException_Default() {
        ValidationException ex = new ValidationException();

        assertNotNull(ex.getMessage());
        assertEquals("Validation failed", ex.getMessage());
    }

    @Test
    void testAccessDeniedException_WithMessage() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        assertEquals("Access denied", ex.getMessage());
    }

    @Test
    void testAccessDeniedException_Default() {
        AccessDeniedException ex = new AccessDeniedException();

        assertNotNull(ex.getMessage());
        assertEquals("Access denied", ex.getMessage());
    }

    @Test
    void testInvalidBookingStatusException_WithMessage() {
        InvalidBookingStatusException ex = new InvalidBookingStatusException("Invalid status");

        assertEquals("Invalid status", ex.getMessage());
    }

    @Test
    void testInvalidBookingStatusException_WithStatusAndOperation() {
        InvalidBookingStatusException ex = new InvalidBookingStatusException(BookingStatus.CANCELLED, "confirm");

        assertNotNull(ex.getMessage());
    }

    @Test
    void testInvalidBookingStatusException_Default() {
        InvalidBookingStatusException ex = new InvalidBookingStatusException();

        assertNotNull(ex.getMessage());
        assertEquals("Invalid booking status", ex.getMessage());
    }

    @Test
    void testRoomNotAvailableException_WithMessage() {
        RoomNotAvailableException ex = new RoomNotAvailableException("Room not available");

        assertEquals("Room not available", ex.getMessage());
    }

    @Test
    void testRoomNotAvailableException_WithId() {
        RoomNotAvailableException ex = new RoomNotAvailableException(1L);

        assertNotNull(ex.getMessage());
        assertEquals("Room is not available: 1", ex.getMessage());
    }

    @Test
    void testRoomNotAvailableException_Default() {
        RoomNotAvailableException ex = new RoomNotAvailableException();

        assertNotNull(ex.getMessage());
        assertEquals("Room is not available", ex.getMessage());
    }

    @Test
    void testBookingException_WithMessage() {
        BookingException ex = new BookingException("Error message") {};

        assertEquals("Error message", ex.getMessage());
    }

    @Test
    void testBookingException_WithMessageAndCause() {
        Throwable cause = new RuntimeException("Cause");
        BookingException ex = new BookingException("Error message", cause) {};

        assertEquals("Error message", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }
}

