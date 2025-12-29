package com.hotelbooking.auth.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hotelbooking.auth.domain.User;
import com.hotelbooking.auth.dto.StaffCreateRequest;
import com.hotelbooking.auth.dto.UserResponse;
import com.hotelbooking.auth.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    /* Create staff */
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

        String token = userService.createStaffUser(user, request.getHotelId());

        return ResponseEntity.status(201)
                .body(Map.of("activationToken", token));
    }

    /* List all users */
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> listUsers() {
        return ResponseEntity.ok(userService.listAllUsers());
    }

    /* Deactivate user */
    @PatchMapping("/users/{userId}/deactivate")
    public ResponseEntity<Void> deactivateUser(
            @PathVariable Long userId,
            @RequestHeader("X-User-Id") Long adminId
    ) {
        userService.deactivateUser(userId, adminId);
        return ResponseEntity.noContent().build();
    }

    /* Reassign staff to hotel */
    @PutMapping("/staff/{userId}/hotel-allotment")
    public ResponseEntity<Void> reassignHotel(
            @PathVariable Long userId,
            @RequestParam Long hotelId
    ) {
        userService.reassignStaffHotel(userId, hotelId);
        return ResponseEntity.noContent().build();
    }
}