package com.hotelbooking.auth.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hotelbooking.auth.domain.Role;
import com.hotelbooking.auth.domain.User;
import com.hotelbooking.auth.exception.UserNotFoundException;
import com.hotelbooking.auth.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class InternalAuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InternalAuthController internalAuthController;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .fullName("Test User")
                .email("test@example.com")
                .role(Role.GUEST)
                .build();
    }

    @Test
    void testGetUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        InternalAuthController.UserInfoResponse response = internalAuthController.getUserById(1L);

        assertNotNull(response);
        assertEquals("test@example.com", response.email());
        assertEquals("testuser", response.username());
    }

    @Test
    void testGetUserById_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> internalAuthController.getUserById(999L));
    }
}

