package com.hotelbooking.auth.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hotelbooking.auth.domain.ActivationToken;

@ExtendWith(MockitoExtension.class)
class ActivationTokenRepositoryTest {

    @Mock
    private ActivationTokenRepository activationTokenRepository;

    private ActivationToken testToken;

    @BeforeEach
    void setUp() {
        testToken = ActivationToken.builder()
                .id(1L)
                .token("test-token-123")
                .userId(1L)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .used(false)
                .build();
    }

    @Test
    void testFindByToken_Success() {
        when(activationTokenRepository.findByToken("test-token-123")).thenReturn(Optional.of(testToken));

        Optional<ActivationToken> found = activationTokenRepository.findByToken("test-token-123");

        assertTrue(found.isPresent());
        assertEquals("test-token-123", found.get().getToken());
    }

    @Test
    void testFindByToken_NotFound() {
        when(activationTokenRepository.findByToken("nonexistent-token")).thenReturn(Optional.empty());

        Optional<ActivationToken> found = activationTokenRepository.findByToken("nonexistent-token");

        assertFalse(found.isPresent());
    }
}

