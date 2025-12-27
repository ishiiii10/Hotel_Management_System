package com.hotelbooking.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotelbooking.auth.domain.ActivationToken;

public interface ActivationTokenRepository
extends JpaRepository<ActivationToken, Long> {

Optional<ActivationToken> findByToken(String token);
}
