package com.hotelbooking.auth.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.hotelbooking.auth.config.JwtProperties;
import com.hotelbooking.auth.domain.Role;
import com.hotelbooking.auth.domain.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties props;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(
                props.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateToken(User user, Long hotelId) {

        var builder = Jwts.builder()
                .subject(user.getEmail())
                .claim("role", user.getRole().name())
                .claim("userId", user.getId())
                .claim("username", user.getUsername())
                .claim("publicUserId", user.getPublicUserId())
                .issuedAt(new Date())
                .expiration(
                        Date.from(
                                Instant.now()
                                        .plus(props.getExpiryMinutes(), ChronoUnit.MINUTES)
                        )
                );

        if (user.getRole() == Role.MANAGER || user.getRole() == Role.RECEPTIONIST) {
            builder.claim("hotelId", hotelId);
        }

        return builder
                .signWith(getSigningKey())
                .compact();
    }

    public Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}