package com.hotelbooking.auth.service;

import org.springframework.stereotype.Service;

import com.hotelbooking.auth.domain.Role;
import com.hotelbooking.auth.domain.User;
import com.hotelbooking.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Self-registration for guests
     */
    public User registerGuest(User user) {
        if (user.getRole() != Role.GUEST) {
            throw new IllegalArgumentException("Only GUEST users can self-register");
        }

        ensureEmailNotExists(user.getEmail());

        return userRepository.save(user);
    }

    /**
     * Admin creates any type of user
     */
    public User createUserByAdmin(User user) {
        ensureEmailNotExists(user.getEmail());
        return userRepository.save(user);
    }

    private void ensureEmailNotExists(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalStateException("Email already exists");
        }
    }
}