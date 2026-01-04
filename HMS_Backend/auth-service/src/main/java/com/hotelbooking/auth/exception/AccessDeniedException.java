package com.hotelbooking.auth.exception;

import com.hotelbooking.auth.domain.AuthErrorCode;

public class AccessDeniedException extends AuthException {
    public AccessDeniedException(String message) {
        super(AuthErrorCode.ACCESS_DENIED, message != null ? message : "Access denied");
    }

    public AccessDeniedException() {
        this("Access denied. You do not have permission to perform this action");
    }
}

