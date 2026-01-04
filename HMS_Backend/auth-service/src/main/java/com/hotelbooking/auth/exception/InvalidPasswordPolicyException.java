package com.hotelbooking.auth.exception;

import com.hotelbooking.auth.domain.AuthErrorCode;

public class InvalidPasswordPolicyException extends AuthException {
    public InvalidPasswordPolicyException(String message) {
        super(AuthErrorCode.INVALID_PASSWORD_POLICY, message != null ? message : "Password does not meet policy requirements");
    }

    public InvalidPasswordPolicyException() {
        this("Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character");
    }
}

