package com.hotelbooking.auth.bootstrap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hotelbooking.auth.config.AdminUserProperties;
import com.hotelbooking.auth.domain.Role;
import com.hotelbooking.auth.domain.User;
import com.hotelbooking.auth.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AdminDataInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdminUserProperties adminProps;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminDataInitializer adminDataInitializer;

    @BeforeEach
    void setUp() {
        when(adminProps.getEmail()).thenReturn("admin@example.com");
    }

    @Test
    void testRun_AdminDoesNotExist_CreatesAdmin() throws Exception {
        when(adminProps.getPassword()).thenReturn("admin123");
        when(adminProps.getFullName()).thenReturn("Admin User");
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("admin123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        adminDataInitializer.run();

        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRun_AdminExists_DoesNotCreate() throws Exception {
        User existingAdmin = User.builder()
                .email("admin@example.com")
                .role(Role.ADMIN)
                .build();

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(existingAdmin));

        adminDataInitializer.run();

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }
}

