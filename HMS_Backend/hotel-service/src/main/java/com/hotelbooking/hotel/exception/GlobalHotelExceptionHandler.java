package com.hotelbooking.hotel.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import jakarta.validation.ConstraintViolationException;
import java.util.Map;

@RestControllerAdvice
public class GlobalHotelExceptionHandler {

    @ExceptionHandler(HotelNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleHotelNotFound(HotelNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "success", false,
                        "message", ex.getMessage(),
                        "error", "HOTEL_NOT_FOUND"
                ));
    }

    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleRoomNotFound(RoomNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "success", false,
                        "message", ex.getMessage(),
                        "error", "ROOM_NOT_FOUND"
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

    @ExceptionHandler(RoomAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleRoomAlreadyExists(RoomAlreadyExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "success", false,
                        "message", ex.getMessage(),
                        "error", "ROOM_ALREADY_EXISTS"
                ));
    }

    @ExceptionHandler(HotelException.class)
    public ResponseEntity<Map<String, Object>> handleHotelException(HotelException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "success", false,
                        "message", ex.getMessage(),
                        "error", "HOTEL_SERVICE_ERROR"
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
                        "error", "VALIDATION_ERROR"
                ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        String message = ex.getMessage();
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String error = "INVALID_STATE";

        if (message != null) {
            if (message.contains("not found") || message.contains("does not exist")) {
                status = HttpStatus.NOT_FOUND;
                error = "NOT_FOUND";
            } else if (message.contains("already exists") || message.contains("duplicate")) {
                status = HttpStatus.CONFLICT;
                error = "ALREADY_EXISTS";
            } else if (message.contains("permission") || message.contains("forbidden") || message.contains("not allowed") || message.contains("Access denied")) {
                status = HttpStatus.FORBIDDEN;
                error = "ACCESS_DENIED";
            }
        }

        return ResponseEntity
                .status(status)
                .body(Map.of(
                        "success", false,
                        "message", message != null ? message : "Invalid state",
                        "error", error
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

