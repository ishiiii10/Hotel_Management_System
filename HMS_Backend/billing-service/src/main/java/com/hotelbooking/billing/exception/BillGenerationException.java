package com.hotelbooking.billing.exception;

public class BillGenerationException extends RuntimeException {
    public BillGenerationException(String message) {
        super(message);
    }

    public BillGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}

