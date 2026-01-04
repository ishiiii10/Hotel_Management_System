package com.hotelbooking.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.hotelbooking.auth.domain.AuthErrorCode;
import com.hotelbooking.auth.dto.ApiError;
import com.hotelbooking.auth.dto.ApiErrorResponse;

import jakarta.validation.ConstraintViolationException;

/**
 * Global exception handler for Auth Service.
 * Handles all custom exceptions and common Spring exceptions.
 */
@RestControllerAdvice
public class GlobalAuthExceptionHandler {

    // ========== Custom Auth Exceptions ==========

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthException(AuthException ex) {
        return ResponseEntity
                .status(resolveHttpStatus(ex.getCode()))
                .body(new ApiErrorResponse(
                        false,
                        new ApiError(
                                ex.getCode().name(),
                                ex.getMessage()
                        )
                ));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ApiErrorResponse(
                        false,
                        new ApiError(
                                AuthErrorCode.INVALID_CREDENTIALS.name(),
                                ex.getMessage()
                        )
                ));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse(
                        false,
                        new ApiError(
                                AuthErrorCode.USER_NOT_FOUND.name(),
                                ex.getMessage()
                        )
                ));
    }

    @ExceptionHandler(CredentialsExpiredException.class)
    public ResponseEntity<ApiErrorResponse> handleCredentialsExpired(CredentialsExpiredException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ApiErrorResponse(
                        false,
                        new ApiError(
                                AuthErrorCode.PASSWORD_EXPIRED.name(),
                                ex.getMessage()
                        )
                ));
    }

    @ExceptionHandler({AccessDeniedException.class, ForbiddenException.class})
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AuthException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ApiErrorResponse(
                        false,
                        new ApiError(
                                AuthErrorCode.ACCESS_DENIED.name(),
                                ex.getMessage()
                        )
                ));
    }

    @ExceptionHandler(InsufficientRoleException.class)
    public ResponseEntity<ApiErrorResponse> handleInsufficientRole(InsufficientRoleException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ApiErrorResponse(
                        false,
                        new ApiError(
                                AuthErrorCode.INSUFFICIENT_ROLE.name(),
                                ex.getMessage()
                        )
                ));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse(
                        false,
                        new ApiError(
                                AuthErrorCode.EMAIL_ALREADY_EXISTS.name(),
                                ex.getMessage()
                        )
                ));
    }

    @ExceptionHandler(InvalidPasswordPolicyException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidPasswordPolicy(InvalidPasswordPolicyException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(
                        false,
                        new ApiError(
                                AuthErrorCode.INVALID_PASSWORD_POLICY.name(),
                                ex.getMessage()
                        )
                ));
    }

    @ExceptionHandler(MissingRequiredFieldException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingRequiredField(MissingRequiredFieldException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(
                        false,
                        new ApiError(
                                AuthErrorCode.MISSING_REQUIRED_FIELD.name(),
                                ex.getMessage()
                        )
                ));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(ValidationException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(
                        false,
                        new ApiError(
                                AuthErrorCode.VALIDATION_ERROR.name(),
                                ex.getMessage()
                        )
                ));
    }

    // ========== Spring Framework Exceptions ==========

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Validation failed");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(
                        false,
                        new ApiError(
                                AuthErrorCode.VALIDATION_ERROR.name(),
                                message
                        )
                ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations()
                .stream()
                .findFirst()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .orElse("Validation constraint violated");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(
                        false,
                        new ApiError(
                                AuthErrorCode.VALIDATION_ERROR.name(),
                                message
                        )
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(
                        false,
                        new ApiError(
                                AuthErrorCode.VALIDATION_ERROR.name(),
                                ex.getMessage() != null ? ex.getMessage() : "Invalid argument provided"
                        )
                ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalState(IllegalStateException ex) {
        // Try to map common IllegalStateException messages to appropriate error codes
        String message = ex.getMessage();
        if (message != null) {
            if (message.contains("not found") || message.contains("does not exist")) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(new ApiErrorResponse(
                                false,
                                new ApiError(
                                        AuthErrorCode.USER_NOT_FOUND.name(),
                                        message
                                )
                        ));
            }
            if (message.contains("already exists") || message.contains("duplicate")) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(new ApiErrorResponse(
                                false,
                                new ApiError(
                                        AuthErrorCode.EMAIL_ALREADY_EXISTS.name(),
                                        message
                                )
                        ));
            }
            if (message.contains("permission") || message.contains("forbidden") || message.contains("not allowed")) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(new ApiErrorResponse(
                                false,
                                new ApiError(
                                        AuthErrorCode.ACCESS_DENIED.name(),
                                        message
                                )
                        ));
            }
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(
                        false,
                        new ApiError(
                                AuthErrorCode.VALIDATION_ERROR.name(),
                                message != null ? message : "Invalid state"
                        )
                ));
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleSpringAccessDenied(
            org.springframework.security.access.AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ApiErrorResponse(
                        false,
                        new ApiError(
                                AuthErrorCode.ACCESS_DENIED.name(),
                                ex.getMessage() != null ? ex.getMessage() : "Access denied"
                        )
                ));
    }

    // ========== Generic Exception Handler (Last Resort) ==========

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex) {
        // Log the exception for debugging (in production, use proper logging)
        ex.printStackTrace();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse(
                        false,
                        new ApiError(
                                AuthErrorCode.INTERNAL_ERROR.name(),
                                "An unexpected error occurred. Please try again later."
                        )
                ));
    }

    // ========== Helper Methods ==========

    private HttpStatus resolveHttpStatus(AuthErrorCode code) {
        return switch (code) {
            case INVALID_CREDENTIALS -> HttpStatus.UNAUTHORIZED;
            case ACCOUNT_DISABLED, PASSWORD_EXPIRED, ACCESS_DENIED, INSUFFICIENT_ROLE -> HttpStatus.FORBIDDEN;
            case EMAIL_ALREADY_EXISTS, USERNAME_ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case USER_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case VALIDATION_ERROR, INVALID_PASSWORD_POLICY, MISSING_REQUIRED_FIELD, INVALID_CURRENT_PASSWORD, WEAK_PASSWORD -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}