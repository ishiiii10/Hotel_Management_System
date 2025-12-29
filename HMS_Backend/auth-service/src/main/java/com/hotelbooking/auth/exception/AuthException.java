package com.hotelbooking.auth.exception;

import com.hotelbooking.auth.domain.AuthErrorCode;

public abstract class AuthException extends RuntimeException {
    private final AuthErrorCode code;

    protected AuthException(AuthErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public AuthErrorCode getCode() {
        return code;
    }
}