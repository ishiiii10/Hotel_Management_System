package com.hotelbooking.auth.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.hotelbooking.auth.domain.AuthErrorCode;

class ExceptionClassesTest {

    @Test
    void testInvalidCredentialsException_WithMessage() {
        InvalidCredentialsException ex = new InvalidCredentialsException("Custom message");

        assertEquals(AuthErrorCode.INVALID_CREDENTIALS, ex.getCode());
        assertEquals("Custom message", ex.getMessage());
    }

    @Test
    void testInvalidCredentialsException_Default() {
        InvalidCredentialsException ex = new InvalidCredentialsException();

        assertEquals(AuthErrorCode.INVALID_CREDENTIALS, ex.getCode());
        assertNotNull(ex.getMessage());
    }

    @Test
    void testUserNotFoundException_WithMessage() {
        UserNotFoundException ex = new UserNotFoundException("User not found");

        assertEquals(AuthErrorCode.USER_NOT_FOUND, ex.getCode());
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void testUserNotFoundException_Default() {
        UserNotFoundException ex = new UserNotFoundException();

        assertEquals(AuthErrorCode.USER_NOT_FOUND, ex.getCode());
        assertNotNull(ex.getMessage());
    }

    @Test
    void testUserAlreadyExistsException_Email() {
        UserAlreadyExistsException ex = new UserAlreadyExistsException("email", "test@example.com");

        assertEquals(AuthErrorCode.EMAIL_ALREADY_EXISTS, ex.getCode());
        assertNotNull(ex.getMessage());
    }

    @Test
    void testUserAlreadyExistsException_Username() {
        UserAlreadyExistsException ex = new UserAlreadyExistsException("username", "testuser");

        assertEquals(AuthErrorCode.EMAIL_ALREADY_EXISTS, ex.getCode());
        assertNotNull(ex.getMessage());
    }

    @Test
    void testValidationException_WithMessage() {
        ValidationException ex = new ValidationException("Validation failed");

        assertEquals(AuthErrorCode.VALIDATION_ERROR, ex.getCode());
        assertEquals("Validation failed", ex.getMessage());
    }

    @Test
    void testValidationException_WithField() {
        ValidationException ex = new ValidationException("field", "Field is required");

        assertEquals(AuthErrorCode.VALIDATION_ERROR, ex.getCode());
        assertNotNull(ex.getMessage());
    }

    @Test
    void testMissingRequiredFieldException() {
        MissingRequiredFieldException ex = new MissingRequiredFieldException("fieldName");

        assertEquals(AuthErrorCode.MISSING_REQUIRED_FIELD, ex.getCode());
        assertNotNull(ex.getMessage());
    }

    @Test
    void testInvalidPasswordPolicyException_WithMessage() {
        InvalidPasswordPolicyException ex = new InvalidPasswordPolicyException("Weak password");

        assertEquals(AuthErrorCode.INVALID_PASSWORD_POLICY, ex.getCode());
        assertEquals("Weak password", ex.getMessage());
    }

    @Test
    void testInvalidPasswordPolicyException_Default() {
        InvalidPasswordPolicyException ex = new InvalidPasswordPolicyException();

        assertEquals(AuthErrorCode.INVALID_PASSWORD_POLICY, ex.getCode());
        assertNotNull(ex.getMessage());
    }

    @Test
    void testInsufficientRoleException() {
        InsufficientRoleException ex = new InsufficientRoleException("Insufficient role");

        assertEquals(AuthErrorCode.INSUFFICIENT_ROLE, ex.getCode());
        assertEquals("Insufficient role", ex.getMessage());
    }

    @Test
    void testAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        assertEquals(AuthErrorCode.ACCESS_DENIED, ex.getCode());
        assertEquals("Access denied", ex.getMessage());
    }

    @Test
    void testForbiddenException() {
        ForbiddenException ex = new ForbiddenException("Forbidden");

        assertEquals(AuthErrorCode.ACCESS_DENIED, ex.getCode());
        assertEquals("Forbidden", ex.getMessage());
    }

    @Test
    void testCredentialsExpiredException_WithMessage() {
        CredentialsExpiredException ex = new CredentialsExpiredException("Credentials expired");

        assertEquals(AuthErrorCode.PASSWORD_EXPIRED, ex.getCode());
        assertEquals("Credentials expired", ex.getMessage());
    }

    @Test
    void testCredentialsExpiredException_Default() {
        CredentialsExpiredException ex = new CredentialsExpiredException();

        assertEquals(AuthErrorCode.PASSWORD_EXPIRED, ex.getCode());
        assertNotNull(ex.getMessage());
    }

    @Test
    void testAccountDisabledException() {
        AccountDisabledException ex = new AccountDisabledException();

        assertEquals(AuthErrorCode.ACCOUNT_DISABLED, ex.getCode());
        assertNotNull(ex.getMessage());
    }

    @Test
    void testPasswordExpiredException() {
        PasswordExpiredException ex = new PasswordExpiredException();

        assertEquals(AuthErrorCode.PASSWORD_EXPIRED, ex.getCode());
        assertNotNull(ex.getMessage());
    }
}

