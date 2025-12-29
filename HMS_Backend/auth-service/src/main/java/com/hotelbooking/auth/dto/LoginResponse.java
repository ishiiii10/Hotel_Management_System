package com.hotelbooking.auth.dto;

import com.hotelbooking.auth.domain.Role;
import lombok.Getter;

@Getter
public class LoginResponse {

    private final String token;
    private final String email;
    private final Role role;
    private final Long hotelId; // null for ADMIN & GUEST

    public LoginResponse(String token, String email, Role role, Long hotelId) {
        this.token = token;
        this.email = email;
        this.role = role;
        this.hotelId = hotelId;
    }
}