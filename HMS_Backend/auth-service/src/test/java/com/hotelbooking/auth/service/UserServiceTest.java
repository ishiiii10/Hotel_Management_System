package com.hotelbooking.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.hotelbooking.auth.domain.Role;
import com.hotelbooking.auth.domain.User;
import com.hotelbooking.auth.repository.ActivationTokenRepository;
import com.hotelbooking.auth.repository.UserHotelAssignmentRepository;
import com.hotelbooking.auth.repository.UserRepository;
import com.hotelbooking.auth.support.AbstractAuthTest;
import com.hotelbooking.auth.support.TestDataFactory;

import org.springframework.security.crypto.password.PasswordEncoder;

class UserServiceTest extends AbstractAuthTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserHotelAssignmentRepository userHotelAssignmentRepository;

    @Mock
    private ActivationTokenRepository activationTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void registerGuest_success() {
        User guest = TestDataFactory.guestUser();

        when(userRepository.findByEmail(guest.getEmail()))
                .thenReturn(Optional.empty());
        when(userRepository.existsByUsername(guest.getUsername()))
                .thenReturn(false);
        when(passwordEncoder.encode(anyString()))
                .thenReturn("hashed-password");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User saved = userService.registerGuest(guest);

        assertNotNull(saved);
        assertEquals(Role.GUEST, saved.getRole());
        assertTrue(saved.isEnabled());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void authenticate_success() {
        User user = TestDataFactory.guestUser();

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(true);

        User authenticated =
                userService.authenticate(user.getEmail(), "Password1");

        assertNotNull(authenticated);
        assertEquals(user.getEmail(), authenticated.getEmail());
    }

    @Test
    void createStaffUser_success() {
        User staff = TestDataFactory.managerUser(1L);

        when(userRepository.findByEmail(staff.getEmail()))
                .thenReturn(Optional.empty());
        when(userRepository.existsByUsername(staff.getUsername()))
                .thenReturn(false);
        when(passwordEncoder.encode(anyString()))
                .thenReturn("hashed-password");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User u = invocation.getArgument(0);
                    u.setId(10L);
                    return u;
                });

        String token = userService.createStaffUser(staff, 1L);

        assertNotNull(token);
        verify(userRepository).save(any(User.class));
        verify(userHotelAssignmentRepository).save(any());
        verify(activationTokenRepository).save(any());
    }

    @Test
    void changePassword_success() {
        User user = TestDataFactory.guestUser();

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(true);
        when(passwordEncoder.encode(anyString()))
                .thenReturn("new-hashed-password");

        userService.changePassword(
                user.getId(),
                "oldPass",
                "NewPass@123"
        );

        verify(userRepository).save(user);
    }
}