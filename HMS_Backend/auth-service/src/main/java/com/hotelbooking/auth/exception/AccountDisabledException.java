package com.hotelbooking.auth.exception;

import com.hotelbooking.auth.domain.AuthErrorCode;

public class AccountDisabledException
extends AuthException {
public AccountDisabledException() {
super(AuthErrorCode.ACCOUNT_DISABLED,
      "Your account has been deactivated");
}
}