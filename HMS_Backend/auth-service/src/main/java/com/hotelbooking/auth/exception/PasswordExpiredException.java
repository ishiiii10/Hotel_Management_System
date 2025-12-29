package com.hotelbooking.auth.exception;

import com.hotelbooking.auth.domain.AuthErrorCode;

public class PasswordExpiredException
extends AuthException {
public PasswordExpiredException() {
super(AuthErrorCode.PASSWORD_EXPIRED,
      "Password expired. Please change password.");
}
}