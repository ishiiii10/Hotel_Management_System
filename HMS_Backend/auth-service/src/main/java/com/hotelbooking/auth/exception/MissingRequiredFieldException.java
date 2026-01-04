package com.hotelbooking.auth.exception;

import com.hotelbooking.auth.domain.AuthErrorCode;

public class MissingRequiredFieldException extends AuthException {
    public MissingRequiredFieldException(String fieldName) {
        super(AuthErrorCode.MISSING_REQUIRED_FIELD, 
              fieldName != null ? "Required field is missing: " + fieldName : "Required field is missing");
    }

    public MissingRequiredFieldException() {
        super(AuthErrorCode.MISSING_REQUIRED_FIELD, "One or more required fields are missing");
    }
}

