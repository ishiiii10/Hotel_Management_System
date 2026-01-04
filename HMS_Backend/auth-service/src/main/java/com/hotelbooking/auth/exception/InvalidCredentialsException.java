package com.hotelbooking.auth.exception;

import com.hotelbooking.auth.domain.AuthErrorCode;

public class InvalidCredentialsException extends AuthException {
    public InvalidCredentialsException(String message) {
        super(AuthErrorCode.INVALID_CREDENTIALS, 
              message != null ? message : "Email or password is incorrect");
    }

    public InvalidCredentialsException() {
        this("Email or password is incorrect");
    }
}