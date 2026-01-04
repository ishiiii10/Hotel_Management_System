package com.hotelbooking.auth.exception;

/**
 * Alias for AccessDeniedException - both map to 403 Forbidden
 */
public class ForbiddenException extends AccessDeniedException {
    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException() {
        super("Forbidden. You do not have permission to access this resource");
    }
}

