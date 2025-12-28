package com.hotelbooking.auth.dto;

import com.hotelbooking.auth.domain.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

    private Long userId;
    private String email;
    private Role role;
}