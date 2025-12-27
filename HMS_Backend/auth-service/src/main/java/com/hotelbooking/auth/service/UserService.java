package com.hotelbooking.auth.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
    
   
    @Transactional
    public String createStaffUser(
            User user,
            List<Long> hotelIds
    ) {

        if (user.getRole() != Role.MANAGER && user.getRole() != Role.RECEPTIONIST) {
            throw new IllegalArgumentException("Only MANAGER or RECEPTIONIST can be created via this flow");
        }

        ensureEmailNotExists(user.getEmail());

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
}