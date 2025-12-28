package com.hotelbooking.auth.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hotelbooking.auth.domain.ActivationToken;
import com.hotelbooking.auth.domain.Role;
import com.hotelbooking.auth.domain.User;
import com.hotelbooking.auth.domain.UserHotelAssignment;
import com.hotelbooking.auth.repository.ActivationTokenRepository;
import com.hotelbooking.auth.repository.UserHotelAssignmentRepository;
import com.hotelbooking.auth.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserHotelAssignmentRepository userHotelAssignmentRepository;
    private final ActivationTokenRepository activationTokenRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Self-registration for guests
     */
    public User registerGuest(User user) {
        if (user.getRole() != Role.GUEST) {
            throw new IllegalArgumentException("Only GUEST users can self-register");
        }

        ensureEmailNotExists(user.getEmail());

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        return userRepository.save(user);
    }

    /**
     * Admin creates any type of user
     */
    public User createUserByAdmin(User user) {
        ensureEmailNotExists(user.getEmail());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    private void ensureEmailNotExists(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalStateException("Email already exists");
        }
    }
    
   
    @Transactional
    public String createStaffUser(
            User user,
            List<Long> hotelIds
    ) {

        if (user.getRole() != Role.MANAGER && user.getRole() != Role.RECEPTIONIST) {
            throw new IllegalArgumentException("Only MANAGER or RECEPTIONIST can be created via this flow");
        }

        ensureEmailNotExists(user.getEmail());
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(false);
        User savedUser = userRepository.save(user);

        hotelIds.forEach(hotelId -> {
            UserHotelAssignment assignment = UserHotelAssignment.builder()
                    .userId(savedUser.getId())
                    .hotelId(hotelId)
                    .build();
            userHotelAssignmentRepository.save(assignment);
        });

        return generateActivationToken(savedUser.getId());
    }
    public String generateActivationToken(Long userId) {

        String token = UUID.randomUUID().toString().replace("-", "");

        ActivationToken activationToken = ActivationToken.builder()
                .token(token)
                .userId(userId)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .used(false)
                .build();

        activationTokenRepository.save(activationToken);

        return token;
    }
    
    @Transactional
    public void activateUser(String token) {

        ActivationToken activationToken = activationTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid activation token"));

        if (activationToken.isUsed()) {
            throw new IllegalStateException("Activation token already used");
        }

        if (activationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Activation token has expired");
        }

        User user = userRepository.findById(activationToken.getUserId())
                .orElseThrow(() -> new IllegalStateException("User not found for token"));

        if (user.isEnabled()) {
            throw new IllegalStateException("User is already activated");
        }

        // Activate user
        user.setEnabled(true);
        userRepository.save(user);

        // Invalidate token
        activationToken.setUsed(true);
        activationTokenRepository.save(activationToken);
    }
    
    public User authenticate(String email, String rawPassword) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!user.isEnabled()) {
            throw new IllegalStateException("Account is not activated");
        }

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        return user;
    }    
}