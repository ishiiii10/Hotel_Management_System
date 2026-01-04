package com.hotelbooking.auth.support;


import java.time.LocalDateTime;
import java.util.UUID;

import com.hotelbooking.auth.domain.ActivationToken;
import com.hotelbooking.auth.domain.Role;
import com.hotelbooking.auth.domain.User;
import com.hotelbooking.auth.dto.GuestRegisterRequest;
import com.hotelbooking.auth.dto.LoginRequest;
import com.hotelbooking.auth.dto.StaffCreateRequest;

public final class TestDataFactory {

    private TestDataFactory() {}
    
    //USER
    
    public static User guestUser() {
        return User.builder()
                .id(1L)
                .fullName("Test Guest")
                .username("guest_user")
                .email("guest@test.com")
                .password("$2a$10$hashedpassword") // dummy bcrypt
                .role(Role.GUEST)
                .enabled(true)
                .passwordLastChangedAt(LocalDateTime.now())
                .build();
    }

    public static User adminUser() {
        return User.builder()
                .id(2L)
                .fullName("Admin User")
                .username("admin")
                .email("admin@test.com")
                .password("$2a$10$hashedpassword")
                .role(Role.ADMIN)
                .enabled(true)
                .passwordLastChangedAt(LocalDateTime.now())
                .build();
    }

    public static User managerUser(Long hotelId) {
        return User.builder()
                .id(3L)
                .fullName("Manager User")
                .username("manager1")
                .email("manager@test.com")
                .password("$2a$10$hashedpassword")
                .role(Role.MANAGER)
                .hotelId(hotelId)
                .enabled(true)
                .passwordLastChangedAt(LocalDateTime.now())
                .build();
    }

    public static User disabledUser() {
        User user = guestUser();
        user.setEnabled(false);
        return user;
    }

    public static User expiredPasswordUser() {
        User user = guestUser();
        user.setPasswordLastChangedAt(LocalDateTime.now().minusDays(200));
        return user;
    }

    //TOKENS

    public static ActivationToken validActivationToken(Long userId) {
        return ActivationToken.builder()
                .id(1L)
                .token(UUID.randomUUID().toString())
                .userId(userId)
                .expiresAt(LocalDateTime.now().plusHours(2))
                .used(false)
                .build();
    }

    public static ActivationToken expiredActivationToken(Long userId) {
        return ActivationToken.builder()
                .id(2L)
                .token(UUID.randomUUID().toString())
                .userId(userId)
                .expiresAt(LocalDateTime.now().minusHours(1))
                .used(false)
                .build();
    }

    //REQUEST DTOs

    public static GuestRegisterRequest validGuestRegisterRequest() {
        GuestRegisterRequest req = new GuestRegisterRequest();
        req.setFullName("Guest User");
        req.setUsername("guest123");
        req.setEmail("guest123@test.com");
        req.setPassword("Password1");
        return req;
    }

    public static LoginRequest validLoginRequest() {
        return new LoginRequest("guest@test.com", "Password1");
    }

    public static LoginRequest invalidLoginRequest() {
        return new LoginRequest("guest@test.com", "wrong-password");
    }

    public static StaffCreateRequest validStaffCreateRequest(Long hotelId) {
        StaffCreateRequest req = new StaffCreateRequest();
        req.setFullName("Staff User");
        req.setUsername("staff1");
        req.setEmail("staff@test.com");
        req.setPassword("Password1");
        req.setRole("MANAGER");
        req.setHotelId(hotelId);
        return req;
    }
}