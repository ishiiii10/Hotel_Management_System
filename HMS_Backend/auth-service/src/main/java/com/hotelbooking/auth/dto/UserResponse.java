package com.hotelbooking.auth.dto;

import com.hotelbooking.auth.domain.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String publicUserId;
    private String username;
    private String fullName;
    private String email;
    private Role role;
    private boolean enabled;
}