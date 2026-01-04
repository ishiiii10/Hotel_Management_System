package com.hotelbooking.auth.dto;

import java.io.Serializable;

import com.hotelbooking.auth.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminUserResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private String publicUserId;
    private String username;
    private String fullName;
    private String email;
    private Role role;
    private boolean enabled;
    private Long hotelId; // null for GUEST & ADMIN
}