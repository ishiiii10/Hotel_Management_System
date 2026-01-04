package com.hotelbooking.auth.exception;

import com.hotelbooking.auth.domain.AuthErrorCode;

public class UserAlreadyExistsException extends AuthException {
    public UserAlreadyExistsException(String message) {
        super(AuthErrorCode.EMAIL_ALREADY_EXISTS, message != null ? message : "User already exists");
    }

    public UserAlreadyExistsException(String field, String value) {
        this(field + " already exists: " + value);
    }

    public UserAlreadyExistsException() {
        this("User with this email or username already exists");
    }
}

