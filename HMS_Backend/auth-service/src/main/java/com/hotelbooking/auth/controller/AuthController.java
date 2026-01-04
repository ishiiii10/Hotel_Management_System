package com.hotelbooking.auth.controller;


import org.springframework.http.HttpStatus;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotelbooking.auth.domain.Role;
import com.hotelbooking.auth.domain.User;
import com.hotelbooking.auth.dto.ChangePasswordRequest;
import com.hotelbooking.auth.dto.GuestRegisterRequest;
import com.hotelbooking.auth.dto.LoginRequest;
import com.hotelbooking.auth.dto.LoginResponse;
import com.hotelbooking.auth.dto.UserResponse;
import com.hotelbooking.auth.security.JwtUtil;
import com.hotelbooking.auth.service.UserService;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register/guest")
    public ResponseEntity<UserResponse> registerGuest(
            @Valid @RequestBody GuestRegisterRequest request
    ) {
        User user = User.builder()
                .fullName(request.getFullName())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
                .role(Role.GUEST)
                .build();

        User saved = userService.registerGuest(user);

        UserResponse response = new UserResponse(
                saved.getId(),
                saved.getPublicUserId(),
                saved.getUsername(),
                saved.getFullName(),
                saved.getEmail(),
                saved.getRole(),
                saved.isEnabled()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        User user = userService.authenticate(
                request.getEmail(),
                request.getPassword()
        );

        Long hotelId = null;

        if (user.getRole() == Role.MANAGER || user.getRole() == Role.RECEPTIONIST) {
            hotelId = userService.getAssignedHotelId(user.getId());
        }

        String token = jwtUtil.generateToken(user, hotelId);

        return ResponseEntity.ok(
                new LoginResponse(
                        token,
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRole(),
                        hotelId
                )
        );
    }
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(
            @RequestHeader("X-User-Id") Long userId
    ) {
        User user = userService.getUserById(userId);

        return ResponseEntity.ok(
                new UserResponse(
                        user.getId(),
                        user.getPublicUserId(),
                        user.getUsername(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getRole(),
                        user.isEnabled()
                )
        );
    }
    
    
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @RequestBody @Valid ChangePasswordRequest request,
            @RequestHeader("X-User-Id") Long userId
    ) {
        userService.changePassword(
                userId,
                request.getCurrentPassword(),
                request.getNewPassword()
        );
        return ResponseEntity.noContent().build();
    }
}