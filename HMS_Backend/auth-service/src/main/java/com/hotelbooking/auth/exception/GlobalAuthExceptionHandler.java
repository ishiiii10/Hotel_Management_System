package com.hotelbooking.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.hotelbooking.auth.domain.AuthErrorCode;
import com.hotelbooking.auth.dto.ApiError;
import com.hotelbooking.auth.dto.ApiErrorResponse;

@RestControllerAdvice
public class GlobalAuthExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthException(
            AuthException ex) {

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .get(0)
                .getDefaultMessage();

        return ResponseEntity
                .badRequest()
                .body(new ApiErrorResponse(
                        false,
                        new ApiError(
                                AuthErrorCode.VALIDATION_ERROR.name(),
                                message
                        )
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnknown(Exception ex) {

        return ResponseEntity
                .internalServerError()
                .body(new ApiErrorResponse(
                        false,
                        new ApiError(
                                AuthErrorCode.INTERNAL_ERROR.name(),
                                "Unexpected error occurred"
                        )
                ));
    }

    private HttpStatus resolveHttpStatus(AuthErrorCode code) {
        return switch (code) {
            case INVALID_CREDENTIALS -> HttpStatus.UNAUTHORIZED;
            case ACCOUNT_DISABLED, PASSWORD_EXPIRED -> HttpStatus.FORBIDDEN;
            case EMAIL_ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case VALIDATION_ERROR -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}