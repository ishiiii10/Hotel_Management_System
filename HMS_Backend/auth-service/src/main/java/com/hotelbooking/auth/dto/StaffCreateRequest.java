package com.hotelbooking.auth.dto;

import java.util.List;

import com.hotelbooking.auth.domain.Role;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaffCreateRequest {

    @NotBlank
    private String fullName;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotNull
    private Role role; // MANAGER or RECEPTIONIST

    @NotEmpty
    private List<Long> hotelIds;
}