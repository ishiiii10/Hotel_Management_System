package com.hotelbooking.auth.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.hotelbooking.auth.domain.AuthErrorCode;
import com.hotelbooking.auth.dto.ApiErrorResponse;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;

class GlobalAuthExceptionHandlerTest {

    private GlobalAuthExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalAuthExceptionHandler();
    }

    @Test
    void testHandleInvalidCredentials() {
        InvalidCredentialsException ex = new InvalidCredentialsException("Invalid credentials");

        ResponseEntity<ApiErrorResponse> response = handler.handleInvalidCredentials(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().error());
        assertEquals(AuthErrorCode.INVALID_CREDENTIALS.name(), response.getBody().error().code());
    }

    @Test
    void testHandleUserNotFound() {
        UserNotFoundException ex = new UserNotFoundException("User not found");

        ResponseEntity<ApiErrorResponse> response = handler.handleUserNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().error());
        assertEquals(AuthErrorCode.USER_NOT_FOUND.name(), response.getBody().error().code());
    }

    @Test
    void testHandleUserAlreadyExists() {
        UserAlreadyExistsException ex = new UserAlreadyExistsException("email", "test@example.com");

        ResponseEntity<ApiErrorResponse> response = handler.handleUserAlreadyExists(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleValidationException() {
        ValidationException ex = new ValidationException("Validation failed");

        ResponseEntity<ApiErrorResponse> response = handler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().error());
        assertEquals(AuthErrorCode.VALIDATION_ERROR.name(), response.getBody().error().code());
    }

    @Test
    void testHandleInvalidPasswordPolicy() {
        InvalidPasswordPolicyException ex = new InvalidPasswordPolicyException("Weak password");

        ResponseEntity<ApiErrorResponse> response = handler.handleInvalidPasswordPolicy(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");

        ResponseEntity<ApiErrorResponse> response = handler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleIllegalStateException_NotFound() {
        IllegalStateException ex = new IllegalStateException("User not found");

        ResponseEntity<ApiErrorResponse> response = handler.handleIllegalState(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleIllegalStateException_AlreadyExists() {
        IllegalStateException ex = new IllegalStateException("User already exists");

        ResponseEntity<ApiErrorResponse> response = handler.handleIllegalState(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleGenericException() {
        Exception ex = new Exception("Unexpected error");

        ResponseEntity<ApiErrorResponse> response = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().error());
    }

    @Test
    void testHandleAccountDisabledException() {
        AccountDisabledException ex = new AccountDisabledException();

        ResponseEntity<ApiErrorResponse> response = handler.handleAuthException(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandlePasswordExpiredException() {
        PasswordExpiredException ex = new PasswordExpiredException();

        ResponseEntity<ApiErrorResponse> response = handler.handleAuthException(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        ResponseEntity<ApiErrorResponse> response = handler.handleAccessDenied(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleForbiddenException() {
        ForbiddenException ex = new ForbiddenException("Forbidden");

        ResponseEntity<ApiErrorResponse> response = handler.handleAccessDenied(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleInsufficientRoleException() {
        InsufficientRoleException ex = new InsufficientRoleException("Insufficient role");

        ResponseEntity<ApiErrorResponse> response = handler.handleInsufficientRole(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleMissingRequiredFieldException() {
        MissingRequiredFieldException ex = new MissingRequiredFieldException("fieldName");

        ResponseEntity<ApiErrorResponse> response = handler.handleMissingRequiredField(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleMethodArgumentNotValidException() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "Error message");
        List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(fieldError);

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        ResponseEntity<ApiErrorResponse> response = handler.handleMethodArgumentNotValid(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleMethodArgumentNotValidException_NoFieldErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(new ArrayList<>());

        ResponseEntity<ApiErrorResponse> response = handler.handleMethodArgumentNotValid(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleConstraintViolationException() {
        ConstraintViolationException ex = mock(ConstraintViolationException.class);
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);

        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("Constraint violated");
        when(path.toString()).thenReturn("field");
        violations.add(violation);

        when(ex.getConstraintViolations()).thenReturn(violations);

        ResponseEntity<ApiErrorResponse> response = handler.handleConstraintViolation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleConstraintViolationException_NoViolations() {
        ConstraintViolationException ex = mock(ConstraintViolationException.class);

        when(ex.getConstraintViolations()).thenReturn(new HashSet<>());

        ResponseEntity<ApiErrorResponse> response = handler.handleConstraintViolation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleIllegalStateException_Permission() {
        IllegalStateException ex = new IllegalStateException("not allowed");

        ResponseEntity<ApiErrorResponse> response = handler.handleIllegalState(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleIllegalStateException_Default() {
        IllegalStateException ex = new IllegalStateException("Some error");

        ResponseEntity<ApiErrorResponse> response = handler.handleIllegalState(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleIllegalStateException_NullMessage() {
        IllegalStateException ex = new IllegalStateException((String) null);

        ResponseEntity<ApiErrorResponse> response = handler.handleIllegalState(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleSpringAccessDeniedException() {
        org.springframework.security.access.AccessDeniedException ex = 
            new org.springframework.security.access.AccessDeniedException("Access denied");

        ResponseEntity<ApiErrorResponse> response = handler.handleSpringAccessDenied(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleSpringAccessDeniedException_NullMessage() {
        org.springframework.security.access.AccessDeniedException ex = 
            new org.springframework.security.access.AccessDeniedException(null);

        ResponseEntity<ApiErrorResponse> response = handler.handleSpringAccessDenied(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleIllegalArgumentException_NullMessage() {
        IllegalArgumentException ex = new IllegalArgumentException((String) null);

        ResponseEntity<ApiErrorResponse> response = handler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}

