package com.hotelbooking.auth.dto;

import com.hotelbooking.auth.domain.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

	private String token;
    private String email;
    private Role role;
}