package com.hotelbooking.auth.exception;

import com.hotelbooking.auth.domain.AuthErrorCode;

public class ValidationException extends AuthException {
    public ValidationException(String message) {
        super(AuthErrorCode.VALIDATION_ERROR, message != null ? message : "Validation failed");
    }

    public ValidationException(String fieldName, String reason) {
        this("Validation failed for field '" + fieldName + "': " + reason);
    }

    public ValidationException() {
        this("Validation failed. Please check your input");
    }
}

