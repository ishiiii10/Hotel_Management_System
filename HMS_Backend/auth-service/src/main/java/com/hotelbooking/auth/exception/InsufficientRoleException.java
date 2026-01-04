package com.hotelbooking.auth.exception;

import com.hotelbooking.auth.domain.AuthErrorCode;

public class InsufficientRoleException extends AuthException {
    public InsufficientRoleException(String message) {
        super(AuthErrorCode.INSUFFICIENT_ROLE, message != null ? message : "Insufficient role");
    }

    public InsufficientRoleException(String requiredRole, String actualRole) {
        this("Required role: " + requiredRole + ", but user has role: " + actualRole);
    }

    public InsufficientRoleException() {
        this("You do not have the required role to perform this action");
    }
}

