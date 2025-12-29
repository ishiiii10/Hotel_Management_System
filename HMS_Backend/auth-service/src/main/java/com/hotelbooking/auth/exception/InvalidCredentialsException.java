package com.hotelbooking.auth.exception;

import com.hotelbooking.auth.domain.AuthErrorCode;

public class InvalidCredentialsException
extends AuthException {
public InvalidCredentialsException() {
super(AuthErrorCode.INVALID_CREDENTIALS,
      "Email or password is incorrect");
}
}