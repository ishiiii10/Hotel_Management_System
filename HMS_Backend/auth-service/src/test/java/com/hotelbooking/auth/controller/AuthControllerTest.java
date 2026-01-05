package com.hotelbooking.auth.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.hotelbooking.auth.domain.Role;
import com.hotelbooking.auth.domain.User;
import com.hotelbooking.auth.dto.ChangePasswordRequest;
import com.hotelbooking.auth.dto.GuestRegisterRequest;
import com.hotelbooking.auth.dto.LoginRequest;
import com.hotelbooking.auth.dto.LoginResponse;
import com.hotelbooking.auth.dto.UserResponse;
import com.hotelbooking.auth.security.JwtUtil;
import com.hotelbooking.auth.service.UserService;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .publicUserId("GUEST-ABC123")
                .username("testuser")
                .fullName("Test User")
                .email("test@example.com")
                .role(Role.GUEST)
                .enabled(true)
                .build();
    }

    @Test
    void testRegisterGuest_Success() {
        GuestRegisterRequest request = new GuestRegisterRequest();
        request.setFullName("John Doe");
        request.setUsername("johndoe");
        request.setEmail("john@example.com");
        request.setPassword("Password123!");

        when(userService.registerGuest(any(User.class))).thenReturn(testUser);

        ResponseEntity<UserResponse> response = authController.registerGuest(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("testuser", response.getBody().getUsername());
    }

    @Test
    void testLogin_Success() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userService.authenticate(anyString(), anyString())).thenReturn(testUser);
        when(jwtUtil.generateToken(any(User.class), any())).thenReturn("test-token");

        ResponseEntity<LoginResponse> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test-token", response.getBody().getToken());
    }

    @Test
    void testLogin_WithHotelId() {
        User manager = User.builder()
                .id(2L)
                .username("manager")
                .email("manager@example.com")
                .role(Role.MANAGER)
                .build();

        LoginRequest request = new LoginRequest();
        request.setEmail("manager@example.com");
        request.setPassword("password123");

        when(userService.authenticate(anyString(), anyString())).thenReturn(manager);
        when(userService.getAssignedHotelId(2L)).thenReturn(100L);
        when(jwtUtil.generateToken(any(User.class), any())).thenReturn("manager-token");

        ResponseEntity<LoginResponse> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(100L, response.getBody().getHotelId());
    }

    @Test
    void testGetMe_Success() {
        when(userService.getUserById(1L)).thenReturn(testUser);

        ResponseEntity<UserResponse> response = authController.me(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("testuser", response.getBody().getUsername());
    }

    @Test
    void testChangePassword_Success() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldpass");
        request.setNewPassword("NewPass123!");

        ResponseEntity<Void> response = authController.changePassword(request, 1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testRegisterGuest_UsernameExists() {
        GuestRegisterRequest request = new GuestRegisterRequest();
        request.setFullName("John Doe");
        request.setUsername("existinguser");
        request.setEmail("john@example.com");
        request.setPassword("Password123!");

        when(userService.registerGuest(any(User.class))).thenThrow(new com.hotelbooking.auth.exception.UserAlreadyExistsException("username", "existinguser"));

        assertThrows(com.hotelbooking.auth.exception.UserAlreadyExistsException.class, () -> authController.registerGuest(request));
    }

    @Test
    void testLogin_AccountDisabled() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userService.authenticate(anyString(), anyString())).thenThrow(new com.hotelbooking.auth.exception.AccountDisabledException());

        assertThrows(com.hotelbooking.auth.exception.AccountDisabledException.class, () -> authController.login(request));
    }

    @Test
    void testLogin_InvalidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongpassword");

        when(userService.authenticate(anyString(), anyString())).thenThrow(new com.hotelbooking.auth.exception.InvalidCredentialsException());

        assertThrows(com.hotelbooking.auth.exception.InvalidCredentialsException.class, () -> authController.login(request));
    }

    @Test
    void testGetMe_UserNotFound() {
        when(userService.getUserById(999L)).thenThrow(new com.hotelbooking.auth.exception.UserNotFoundException());

        assertThrows(com.hotelbooking.auth.exception.UserNotFoundException.class, () -> authController.me(999L));
    }

    @Test
    void testChangePassword_InvalidCurrentPassword() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrongpass");
        request.setNewPassword("NewPass123!");

        org.mockito.Mockito.doThrow(new com.hotelbooking.auth.exception.InvalidCredentialsException("Invalid current password"))
                .when(userService).changePassword(anyLong(), anyString(), anyString());

        assertThrows(com.hotelbooking.auth.exception.InvalidCredentialsException.class, () -> authController.changePassword(request, 1L));
    }
}

