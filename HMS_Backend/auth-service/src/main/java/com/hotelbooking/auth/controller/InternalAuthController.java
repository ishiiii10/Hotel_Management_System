package com.hotelbooking.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotelbooking.auth.domain.User;
import com.hotelbooking.auth.exception.UserNotFoundException;
import com.hotelbooking.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Internal controller for inter-service communication via Feign.
 * Returns raw user data without wrapper objects.
 */
@RestController
@RequestMapping("/internal/auth")
@RequiredArgsConstructor
public class InternalAuthController {

    private final UserRepository userRepository;

    @GetMapping("/users/{userId}")
    public UserInfoResponse getUserById(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        
        return new UserInfoResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getFullName(),
                null // Phone number not available in User entity yet
        );
    }

    // Internal DTO for user info
    public record UserInfoResponse(
            Long id,
            String email,
            String username,
            String fullName,
            String phoneNumber
    ) {}
}

