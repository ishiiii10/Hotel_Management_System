package com.hotelbooking.booking.exception;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

class GlobalBookingExceptionHandlerTest {

    private GlobalBookingExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalBookingExceptionHandler();
    }

    @Test
    void testHandleBookingNotFound() {
        BookingNotFoundException ex = new BookingNotFoundException(1L);

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleBookingNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("BOOKING_NOT_FOUND", response.getBody().get("error"));
    }

    @Test
    void testHandleValidationException() {
        ValidationException ex = new ValidationException("Invalid input");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("VALIDATION_ERROR", response.getBody().get("error"));
    }

    @Test
    void testHandleAccessDenied() {
        com.hotelbooking.booking.exception.AccessDeniedException ex = new com.hotelbooking.booking.exception.AccessDeniedException("Access denied");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleAccessDenied(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("ACCESS_DENIED", response.getBody().get("error"));
    }

    @Test
    void testHandleInvalidBookingStatus() {
        InvalidBookingStatusException ex = new InvalidBookingStatusException("Invalid status");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleInvalidBookingStatus(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("INVALID_BOOKING_STATUS", response.getBody().get("error"));
    }

    @Test
    void testHandleRoomNotAvailable() {
        RoomNotAvailableException ex = new RoomNotAvailableException("Room not available");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleRoomNotAvailable(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("ROOM_NOT_AVAILABLE", response.getBody().get("error"));
    }

    @Test
    void testHandleIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("INVALID_ARGUMENT", response.getBody().get("error"));
    }

    @Test
    void testHandleIllegalState_NotFound() {
        IllegalStateException ex = new IllegalStateException("Booking not found");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleIllegalState(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("BOOKING_NOT_FOUND", response.getBody().get("error"));
    }

    @Test
    void testHandleIllegalState_NotAvailable() {
        IllegalStateException ex = new IllegalStateException("Room not available");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleIllegalState(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ROOM_NOT_AVAILABLE", response.getBody().get("error"));
    }

    @Test
    void testHandleIllegalState_AccessDenied() {
        IllegalStateException ex = new IllegalStateException("Access denied");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleIllegalState(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ACCESS_DENIED", response.getBody().get("error"));
    }

    @Test
    void testHandleSpringAccessDenied() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleSpringAccessDenied(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ACCESS_DENIED", response.getBody().get("error"));
    }

    @Test
    void testHandleGenericException() {
        Exception ex = new Exception("Unexpected error");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().get("success"));
        assertEquals("INTERNAL_ERROR", response.getBody().get("error"));
    }

    @Test
    void testHandleIllegalState_Generic() {
        IllegalStateException ex = new IllegalStateException("Some other error");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleIllegalState(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("VALIDATION_ERROR", response.getBody().get("error"));
    }

    @Test
    void testHandleIllegalState_NullMessage() {
        IllegalStateException ex = new IllegalStateException((String) null);

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleIllegalState(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleIllegalArgument_NullMessage() {
        IllegalArgumentException ex = new IllegalArgumentException((String) null);

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INVALID_ARGUMENT", response.getBody().get("error"));
    }

    @Test
    void testHandleSpringAccessDenied_NullMessage() {
        AccessDeniedException ex = new AccessDeniedException((String) null);

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleSpringAccessDenied(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ACCESS_DENIED", response.getBody().get("error"));
    }

    @Test
    void testHandleIllegalState_AlreadyBooked() {
        IllegalStateException ex = new IllegalStateException("Room already booked");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleIllegalState(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ROOM_NOT_AVAILABLE", response.getBody().get("error"));
    }

    @Test
    void testHandleIllegalState_NotAllowed() {
        IllegalStateException ex = new IllegalStateException("Operation not allowed");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleIllegalState(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ACCESS_DENIED", response.getBody().get("error"));
    }

    @Test
    void testHandleMethodArgumentNotValid() {
        org.springframework.validation.FieldError fieldError = new org.springframework.validation.FieldError(
                "createBookingRequest", "checkInDate", "Check-in date is required");
        org.springframework.validation.BindingResult bindingResult = 
                org.mockito.Mockito.mock(org.springframework.validation.BindingResult.class);
        org.springframework.web.bind.MethodArgumentNotValidException ex = 
                org.mockito.Mockito.mock(org.springframework.web.bind.MethodArgumentNotValidException.class);

        when(bindingResult.getFieldErrors()).thenReturn(java.util.Arrays.asList(fieldError));
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleMethodArgumentNotValid(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("VALIDATION_ERROR", response.getBody().get("error"));
    }

    @Test
    void testHandleMethodArgumentNotValid_NoFieldErrors() {
        org.springframework.validation.BindingResult bindingResult = 
                org.mockito.Mockito.mock(org.springframework.validation.BindingResult.class);
        org.springframework.web.bind.MethodArgumentNotValidException ex = 
                org.mockito.Mockito.mock(org.springframework.web.bind.MethodArgumentNotValidException.class);

        when(bindingResult.getFieldErrors()).thenReturn(java.util.Collections.emptyList());
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleMethodArgumentNotValid(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation failed", response.getBody().get("message"));
    }

    @Test
    void testHandleConstraintViolation() {
        jakarta.validation.ConstraintViolation<?> violation = 
                org.mockito.Mockito.mock(jakarta.validation.ConstraintViolation.class);
        jakarta.validation.Path path = org.mockito.Mockito.mock(jakarta.validation.Path.class);
        jakarta.validation.ConstraintViolationException ex = 
                new jakarta.validation.ConstraintViolationException(
                        java.util.Set.of(violation));

        when(violation.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn("checkInDate");
        when(violation.getMessage()).thenReturn("must not be null");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleConstraintViolation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("VALIDATION_ERROR", response.getBody().get("error"));
    }

    @Test
    void testHandleConstraintViolation_EmptyViolations() {
        jakarta.validation.ConstraintViolationException ex = 
                new jakarta.validation.ConstraintViolationException(
                        java.util.Collections.emptySet());

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleConstraintViolation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation constraint violated", response.getBody().get("message"));
    }
}

