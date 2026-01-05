package com.hotelbooking.auth.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hotelbooking.auth.config.JwtProperties;
import com.hotelbooking.auth.domain.Role;
import com.hotelbooking.auth.domain.User;

import io.jsonwebtoken.Claims;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private JwtUtil jwtUtil;

    private User testUser;

    @BeforeEach
    void setUp() {
        when(jwtProperties.getSecret()).thenReturn("test-secret-key-that-is-long-enough-for-hmac-sha-256-algorithm");
        when(jwtProperties.getExpiryMinutes()).thenReturn(60L);

        testUser = User.builder()
                .id(1L)
                .publicUserId("GUEST-ABC123")
                .username("testuser")
                .email("test@example.com")
                .role(Role.GUEST)
                .build();
    }

    @Test
    void testGenerateToken_ForGuest() {
        String token = jwtUtil.generateToken(testUser, null);

        assertNotNull(token);
        
        Claims claims = jwtUtil.validateToken(token);
        assertEquals("test@example.com", claims.getSubject());
        assertEquals("GUEST", claims.get("role"));
        assertEquals(1L, ((Number) claims.get("userId")).longValue());
    }

    @Test
    void testGenerateToken_ForManager() {
        User manager = User.builder()
                .id(2L)
                .publicUserId("MANAGER-XYZ789")
                .username("manager1")
                .email("manager@example.com")
                .role(Role.MANAGER)
                .build();

        String token = jwtUtil.generateToken(manager, 100L);

        assertNotNull(token);
        
        Claims claims = jwtUtil.validateToken(token);
        assertEquals("manager@example.com", claims.getSubject());
        assertEquals("MANAGER", claims.get("role"));
        assertEquals(100L, ((Number) claims.get("hotelId")).longValue());
    }

    @Test
    void testValidateToken_Success() {
        String token = jwtUtil.generateToken(testUser, null);
        
        Claims claims = jwtUtil.validateToken(token);

        assertNotNull(claims);
        assertEquals("test@example.com", claims.getSubject());
    }
}

