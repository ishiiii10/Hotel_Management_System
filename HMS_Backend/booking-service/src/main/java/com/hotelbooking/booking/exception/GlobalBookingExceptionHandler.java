package com.hotelbooking.booking.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalBookingExceptionHandler {

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleBookingNotFound(BookingNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "success", false,
                        "message", ex.getMessage(),
                        "error", "BOOKING_NOT_FOUND"
                ));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(ValidationException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "success", false,
                        "message", ex.getMessage(),
                        "error", "VALIDATION_ERROR"
                ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "success", false,
                        "message", ex.getMessage(),
                        "error", "ACCESS_DENIED"
                ));
    }

    @ExceptionHandler(InvalidBookingStatusException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidBookingStatus(InvalidBookingStatusException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "success", false,
                        "message", ex.getMessage(),
                        "error", "INVALID_BOOKING_STATUS"
                ));
    }

    @ExceptionHandler(RoomNotAvailableException.class)
    public ResponseEntity<Map<String, Object>> handleRoomNotAvailable(RoomNotAvailableException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "success", false,
                        "message", ex.getMessage(),
                        "error", "ROOM_NOT_AVAILABLE"
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Validation failed");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "success", false,
                        "message", message,
                        "error", "VALIDATION_ERROR"
                ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations()
                .stream()
                .findFirst()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .orElse("Validation constraint violated");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "success", false,
                        "message", message,
                        "error", "VALIDATION_ERROR"
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "success", false,
                        "message", ex.getMessage() != null ? ex.getMessage() : "Invalid argument provided",
                        "error", "INVALID_ARGUMENT"
                ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        String message = ex.getMessage();
        if (message != null) {
            if (message.contains("not found")) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "success", false,
                                "message", message,
                                "error", "BOOKING_NOT_FOUND"
                        ));
            }
            if (message.contains("not available") || message.contains("already booked")) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(Map.of(
                                "success", false,
                                "message", message,
                                "error", "ROOM_NOT_AVAILABLE"
                        ));
            }
            if (message.contains("forbidden") || message.contains("access denied") || message.contains("not allowed") || message.contains("can only") || message.contains("Access denied")) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "success", false,
                                "message", message,
                                "error", "ACCESS_DENIED"
                        ));
            }
        }
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "success", false,
                        "message", message != null ? message : "Invalid state",
                        "error", "VALIDATION_ERROR"
                ));
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleSpringAccessDenied(org.springframework.security.access.AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "success", false,
                        "message", ex.getMessage() != null ? ex.getMessage() : "Access denied",
                        "error", "ACCESS_DENIED"
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "success", false,
                        "message", "An unexpected error occurred. Please try again later.",
                        "error", "INTERNAL_ERROR"
                ));
    }
}

