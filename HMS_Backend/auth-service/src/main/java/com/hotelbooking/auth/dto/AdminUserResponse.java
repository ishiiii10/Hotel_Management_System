package com.hotelbooking.auth.dto;

import com.hotelbooking.auth.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminUserResponse {

    private Long userId;
    private String publicUserId;
    private String fullName;
    private String email;
    private Role role;
    private boolean enabled;
    private Long hotelId; // null for GUEST & ADMIN
}