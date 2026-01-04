package com.hotelbooking.auth.exception;

import com.hotelbooking.auth.domain.AuthErrorCode;

public class CredentialsExpiredException extends AuthException {
    public CredentialsExpiredException(String message) {
        super(AuthErrorCode.PASSWORD_EXPIRED, message != null ? message : "Credentials have expired");
    }

    public CredentialsExpiredException() {
        this("Credentials have expired. Please change your password");
    }
}

