package com.hotelbooking.auth.exception;

import com.hotelbooking.auth.domain.AuthErrorCode;

public class UserNotFoundException extends AuthException {
    public UserNotFoundException(String message) {
        super(AuthErrorCode.USER_NOT_FOUND, message != null ? message : "User not found");
    }

    public UserNotFoundException() {
        this("User not found");
    }
}

