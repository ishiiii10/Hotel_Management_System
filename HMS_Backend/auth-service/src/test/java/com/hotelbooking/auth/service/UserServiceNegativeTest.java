package com.hotelbooking.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.hotelbooking.auth.domain.Role;
import com.hotelbooking.auth.domain.User;
import com.hotelbooking.auth.exception.AccountDisabledException;
import com.hotelbooking.auth.exception.CredentialsExpiredException;
import com.hotelbooking.auth.exception.InsufficientRoleException;
import com.hotelbooking.auth.exception.InvalidCredentialsException;
import com.hotelbooking.auth.exception.MissingRequiredFieldException;
import com.hotelbooking.auth.repository.ActivationTokenRepository;
import com.hotelbooking.auth.repository.UserHotelAssignmentRepository;
import com.hotelbooking.auth.repository.UserRepository;
import com.hotelbooking.auth.support.AbstractAuthTest;
import com.hotelbooking.auth.support.TestDataFactory;

import org.springframework.security.crypto.password.PasswordEncoder;

class UserServiceNegativeTest extends AbstractAuthTest {

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
    void registerGuest_whenEmailExists_shouldThrow() {
        User guest = TestDataFactory.guestUser();

        when(userRepository.findByEmail(guest.getEmail()))
                .thenReturn(Optional.of(guest));

        assertThrows(
                RuntimeException.class,
                () -> userService.registerGuest(guest)
        );
    }

    @Test
    void authenticate_whenAccountDisabled_shouldThrow() {
        User disabled = TestDataFactory.disabledUser();

        when(userRepository.findByEmail(disabled.getEmail()))
                .thenReturn(Optional.of(disabled));

        assertThrows(
                AccountDisabledException.class,
                () -> userService.authenticate(disabled.getEmail(), "pass")
        );
    }

    @Test
    void authenticate_whenPasswordExpired_shouldThrow() {
        User expired = TestDataFactory.expiredPasswordUser();

        when(userRepository.findByEmail(expired.getEmail()))
                .thenReturn(Optional.of(expired));

        assertThrows(
                CredentialsExpiredException.class,
                () -> userService.authenticate(expired.getEmail(), "pass")
        );
    }

    @Test
    void authenticate_whenPasswordMismatch_shouldThrow() {
        User user = TestDataFactory.guestUser();

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> userService.authenticate(user.getEmail(), "wrong")
        );
    }

    @Test
    void createStaffUser_whenRoleInvalid_shouldThrow() {
        User guest = TestDataFactory.guestUser();

        assertThrows(
                InsufficientRoleException.class,
                () -> userService.createStaffUser(guest, 1L)
        );
    }

    @Test
    void createStaffUser_whenHotelIdMissing_shouldThrow() {
        User manager = TestDataFactory.managerUser(null);

        assertThrows(
                MissingRequiredFieldException.class,
                () -> userService.createStaffUser(manager, null)
        );
    }

    @Test
    void changePassword_whenCurrentPasswordWrong_shouldThrow() {
        User user = TestDataFactory.guestUser();

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> userService.changePassword(
                        user.getId(),
                        "wrong",
                        "NewPass@123"
                )
        );
    }
}