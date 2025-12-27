package com.hotelbooking.auth.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotelbooking.auth.domain.User;
import com.hotelbooking.auth.dto.StaffCreateRequest;
import com.hotelbooking.auth.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @PostMapping("/staff")
    public ResponseEntity<Map<String, String>> createStaff(
            @Valid @RequestBody StaffCreateRequest request
    ) {
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(request.getPassword())
                .role(request.getRole())
                .build();

        String token = userService.createStaffUser(user, request.getHotelIds());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("activationToken", token));
    }
}
