package com.hotelbooking.hotel.exception;

import org.springframework.http.HttpStatus;
import com.hotelbooking.hotel.exception.HotelErrorCode;

public class HotelException extends RuntimeException {
    private final HotelErrorCode code;
    private final HttpStatus status;

    public HotelException(HotelErrorCode code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public HotelErrorCode getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
