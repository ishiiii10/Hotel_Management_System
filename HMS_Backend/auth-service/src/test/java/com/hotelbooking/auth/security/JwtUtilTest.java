package com.hotelbooking.auth.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.hotelbooking.auth.config.JwtProperties;
import com.hotelbooking.auth.domain.Role;
import com.hotelbooking.auth.domain.User;

import io.jsonwebtoken.Claims;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setup() {
        JwtProperties props = new JwtProperties();
        props.setSecret("this-is-a-very-secure-secret-key-which-is-long-enough");
        props.setExpiryMinutes(60);

        jwtUtil = new JwtUtil(props);
    }

    @Test
    void generateToken_forGuest_shouldNotContainHotelId() {
        User guest = User.builder()
                .id(1L)
                .email("guest@test.com")
                .username("guest")
                .publicUserId("GUEST-123ABC")
                .role(Role.GUEST)
                .build();

        String token = jwtUtil.generateToken(guest, null);

        assertNotNull(token);

        Claims claims = jwtUtil.validateToken(token);

        assertEquals("guest@test.com", claims.getSubject());
        assertEquals("GUEST", claims.get("role"));
        assertEquals(1L, claims.get("userId", Long.class));
        assertNull(claims.get("hotelId"));
    }

    @Test
    void generateToken_forManager_shouldContainHotelId() {
        User manager = User.builder()
                .id(2L)
                .email("manager@test.com")
                .username("manager")
                .publicUserId("MANAGER-ABC123")
                .role(Role.MANAGER)
                .build();

        String token = jwtUtil.generateToken(manager, 99L);

        Claims claims = jwtUtil.validateToken(token);

        assertEquals("MANAGER", claims.get("role"));
        assertEquals(99L, claims.get("hotelId", Long.class));
    }

    @Test
    void tokenShouldExpireInFuture() {
        User user = User.builder()
                .id(3L)
                .email("user@test.com")
                .username("user")
                .publicUserId("USER-XYZ123")
                .role(Role.GUEST)
                .build();

        String token = jwtUtil.generateToken(user, null);

        Claims claims = jwtUtil.validateToken(token);

        Date expiration = claims.getExpiration();

        assertTrue(expiration.after(new Date()));
    }
}