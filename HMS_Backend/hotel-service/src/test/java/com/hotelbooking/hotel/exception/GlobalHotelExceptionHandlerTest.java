package com.hotelbooking.hotel.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;

class GlobalHotelExceptionHandlerTest {

    private GlobalHotelExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalHotelExceptionHandler();
    }

    @Test
    void testHandleHotelNotFound() {
        HotelNotFoundException ex = new HotelNotFoundException(1L);

        ResponseEntity<Map<String, Object>> response = handler.handleHotelNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("HOTEL_NOT_FOUND", response.getBody().get("error"));
    }

    @Test
    void testHandleRoomNotFound() {
        RoomNotFoundException ex = new RoomNotFoundException(1L);

        ResponseEntity<Map<String, Object>> response = handler.handleRoomNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ROOM_NOT_FOUND", response.getBody().get("error"));
    }

    @Test
    void testHandleValidationException() {
        ValidationException ex = new ValidationException("Validation failed");

        ResponseEntity<Map<String, Object>> response = handler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("VALIDATION_ERROR", response.getBody().get("error"));
    }

    @Test
    void testHandleAccessDenied() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        ResponseEntity<Map<String, Object>> response = handler.handleAccessDenied(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ACCESS_DENIED", response.getBody().get("error"));
    }

    @Test
    void testHandleRoomAlreadyExists() {
        RoomAlreadyExistsException ex = new RoomAlreadyExistsException("101", 1L);

        ResponseEntity<Map<String, Object>> response = handler.handleRoomAlreadyExists(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ROOM_ALREADY_EXISTS", response.getBody().get("error"));
    }

    @Test
    void testHandleHotelException() {
        HotelException ex = new HotelException("Generic error") {};

        ResponseEntity<Map<String, Object>> response = handler.handleHotelException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("HOTEL_SERVICE_ERROR", response.getBody().get("error"));
    }

    @Test
    void testHandleMethodArgumentNotValid() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "Error message");
        ArrayList<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(fieldError);

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        ResponseEntity<Map<String, Object>> response = handler.handleMethodArgumentNotValid(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleMethodArgumentNotValid_NoFieldErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(new ArrayList<>());

        ResponseEntity<Map<String, Object>> response = handler.handleMethodArgumentNotValid(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleConstraintViolation() {
        ConstraintViolationException ex = mock(ConstraintViolationException.class);
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);

        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("Constraint violated");
        when(path.toString()).thenReturn("field");
        violations.add(violation);

        when(ex.getConstraintViolations()).thenReturn(violations);

        ResponseEntity<Map<String, Object>> response = handler.handleConstraintViolation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleConstraintViolation_NoViolations() {
        ConstraintViolationException ex = mock(ConstraintViolationException.class);

        when(ex.getConstraintViolations()).thenReturn(new HashSet<>());

        ResponseEntity<Map<String, Object>> response = handler.handleConstraintViolation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");

        ResponseEntity<Map<String, Object>> response = handler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleIllegalArgument_NullMessage() {
        IllegalArgumentException ex = new IllegalArgumentException((String) null);

        ResponseEntity<Map<String, Object>> response = handler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleIllegalState_NotFound() {
        IllegalStateException ex = new IllegalStateException("Hotel not found");

        ResponseEntity<Map<String, Object>> response = handler.handleIllegalState(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("NOT_FOUND", response.getBody().get("error"));
    }

    @Test
    void testHandleIllegalState_AlreadyExists() {
        IllegalStateException ex = new IllegalStateException("Room already exists");

        ResponseEntity<Map<String, Object>> response = handler.handleIllegalState(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ALREADY_EXISTS", response.getBody().get("error"));
    }

    @Test
    void testHandleIllegalState_AccessDenied() {
        IllegalStateException ex = new IllegalStateException("Access denied");

        ResponseEntity<Map<String, Object>> response = handler.handleIllegalState(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ACCESS_DENIED", response.getBody().get("error"));
    }

    @Test
    void testHandleIllegalState_Default() {
        IllegalStateException ex = new IllegalStateException("Some error");

        ResponseEntity<Map<String, Object>> response = handler.handleIllegalState(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INVALID_STATE", response.getBody().get("error"));
    }

    @Test
    void testHandleIllegalState_NullMessage() {
        IllegalStateException ex = new IllegalStateException((String) null);

        ResponseEntity<Map<String, Object>> response = handler.handleIllegalState(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleGenericException() {
        Exception ex = new Exception("Unexpected error");

        ResponseEntity<Map<String, Object>> response = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_ERROR", response.getBody().get("error"));
    }
}

