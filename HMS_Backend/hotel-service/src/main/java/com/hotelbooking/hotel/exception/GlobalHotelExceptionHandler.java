package com.hotelbooking.hotel.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalHotelExceptionHandler {
    @ExceptionHandler(HotelException.class)
    public ResponseEntity<Object> handleHotelException(HotelException ex) {
        return ResponseEntity.status(ex.getStatus()).body(
            new ErrorResponse(ex.getCode().name(), ex.getMessage())
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnknown(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            new ErrorResponse("INTERNAL_ERROR", "Unexpected error occurred")
        );
    }

    public static class ErrorResponse {
        public final String code;
        public final String message;
        public ErrorResponse(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
