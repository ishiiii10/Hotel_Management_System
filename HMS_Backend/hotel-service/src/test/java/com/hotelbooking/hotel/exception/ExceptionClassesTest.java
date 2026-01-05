package com.hotelbooking.hotel.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class ExceptionClassesTest {

    @Test
    void testHotelNotFoundException_WithMessage() {
        HotelNotFoundException ex = new HotelNotFoundException("Hotel not found");

        assertEquals("Hotel not found", ex.getMessage());
    }

    @Test
    void testHotelNotFoundException_WithId() {
        HotelNotFoundException ex = new HotelNotFoundException(1L);

        assertNotNull(ex.getMessage());
        assertEquals("Hotel not found: 1", ex.getMessage());
    }

    @Test
    void testHotelNotFoundException_Default() {
        HotelNotFoundException ex = new HotelNotFoundException();

        assertNotNull(ex.getMessage());
        assertEquals("Hotel not found", ex.getMessage());
    }

    @Test
    void testRoomNotFoundException_WithMessage() {
        RoomNotFoundException ex = new RoomNotFoundException("Room not found");

        assertEquals("Room not found", ex.getMessage());
    }

    @Test
    void testRoomNotFoundException_WithId() {
        RoomNotFoundException ex = new RoomNotFoundException(1L);

        assertNotNull(ex.getMessage());
        assertEquals("Room not found: 1", ex.getMessage());
    }

    @Test
    void testRoomNotFoundException_Default() {
        RoomNotFoundException ex = new RoomNotFoundException();

        assertNotNull(ex.getMessage());
        assertEquals("Room not found", ex.getMessage());
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
    void testRoomAlreadyExistsException_WithMessage() {
        RoomAlreadyExistsException ex = new RoomAlreadyExistsException("Room already exists");

        assertEquals("Room already exists", ex.getMessage());
    }

    @Test
    void testRoomAlreadyExistsException_WithRoomNumberAndHotelId() {
        RoomAlreadyExistsException ex = new RoomAlreadyExistsException("101", 1L);

        assertNotNull(ex.getMessage());
    }

    @Test
    void testRoomAlreadyExistsException_Default() {
        RoomAlreadyExistsException ex = new RoomAlreadyExistsException();

        assertNotNull(ex.getMessage());
        assertEquals("Room already exists", ex.getMessage());
    }

    @Test
    void testHotelException_WithMessage() {
        HotelException ex = new HotelException("Error message") {};

        assertEquals("Error message", ex.getMessage());
    }

    @Test
    void testHotelException_WithMessageAndCause() {
        Throwable cause = new RuntimeException("Cause");
        HotelException ex = new HotelException("Error message", cause) {};

        assertEquals("Error message", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }
}

