package com.hotelbooking.auth.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hotelbooking.auth.domain.ActivationToken;
import com.hotelbooking.auth.domain.Role;
import com.hotelbooking.auth.domain.User;
import com.hotelbooking.auth.domain.UserHotelAssignment;
import com.hotelbooking.auth.dto.AdminUserResponse;
import com.hotelbooking.auth.dto.UserResponse;
import com.hotelbooking.auth.exception.AccountDisabledException;
import com.hotelbooking.auth.exception.CredentialsExpiredException;
import com.hotelbooking.auth.exception.InsufficientRoleException;
import com.hotelbooking.auth.exception.InvalidCredentialsException;
import com.hotelbooking.auth.exception.InvalidPasswordPolicyException;
import com.hotelbooking.auth.exception.MissingRequiredFieldException;
import com.hotelbooking.auth.exception.UserAlreadyExistsException;
import com.hotelbooking.auth.exception.UserNotFoundException;
import com.hotelbooking.auth.exception.ValidationException;
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
    private static final int PASSWORD_EXPIRY_DAYS = 90;

    private static final String STRONG_PASSWORD_REGEX =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).+$";

    /* ---------------- Guest Registration ---------------- */

    @CacheEvict(value = "users", allEntries = true)
    public User registerGuest(User user) {
        if (user.getRole() != Role.GUEST) {
            throw new InsufficientRoleException("Only GUEST users can self-register");
        }

        ensureEmailNotExists(user.getEmail());
        ensureUsernameNotExists(user.getUsername());
        user.setPublicUserId(generatePublicUserId(Role.GUEST));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        return userRepository.save(user);
    }

    /* ---------------- Staff Creation ---------------- */

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public String createStaffUser(User user, Long hotelId) {

        if (user.getRole() != Role.MANAGER && user.getRole() != Role.RECEPTIONIST) {
            throw new InsufficientRoleException("Only MANAGER or RECEPTIONIST can be created");
        }

        if (hotelId == null) {
            throw new MissingRequiredFieldException("hotelId");
        }

        if (user.getRole() == Role.ADMIN) {
            throw new ValidationException("ADMIN role cannot be assigned to a hotel");
        }

        ensureEmailNotExists(user.getEmail());
        ensureUsernameNotExists(user.getUsername());

        user.setPublicUserId(generatePublicUserId(user.getRole()));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setHotelId(hotelId); // Persist hotelId directly in User entity
        user.setEnabled(false);
        User savedUser = userRepository.save(user);

        // Also maintain UserHotelAssignment for backward compatibility
        UserHotelAssignment assignment = UserHotelAssignment.builder()
                .userId(savedUser.getId())
                .hotelId(hotelId)
                .build();

        userHotelAssignmentRepository.save(assignment);

        return generateActivationToken(savedUser.getId());
    }

    /* ---------------- Activation ---------------- */

    @Transactional
    public void activateUser(String token) {

        ActivationToken activationToken = activationTokenRepository.findByToken(token)
                .orElseThrow(() -> new ValidationException("Invalid activation token"));

        if (activationToken.isUsed()) {
            throw new ValidationException("Activation token already used");
        }

        if (activationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new CredentialsExpiredException("Activation token expired");
        }

        User user = userRepository.findById(activationToken.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found for activation token"));

        if (user.isEnabled()) {
            throw new ValidationException("User already activated");
        }

        if (user.getRole() != Role.MANAGER && user.getRole() != Role.RECEPTIONIST) {
            throw new ValidationException("Only staff users require activation");
        }

        userHotelAssignmentRepository.findOneByUserId(user.getId())
                .orElseThrow(() ->
                        new IllegalStateException("Staff user has no hotel assignment")
                );

        user.setEnabled(true);
        userRepository.save(user);

        activationToken.setUsed(true);
        activationTokenRepository.save(activationToken);
    }

    /* ---------------- Authentication ---------------- */

    public User authenticate(String email, String rawPassword) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException());

        if (!user.isEnabled()) {
            throw new AccountDisabledException();
        }
        if (user.getPasswordLastChangedAt()
                .plusDays(PASSWORD_EXPIRY_DAYS)
                .isBefore(LocalDateTime.now())) {

            throw new CredentialsExpiredException();
        }

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        return user;
    }
    
    public Long getAssignedHotelId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());
        
        // Prefer hotelId from User entity, fallback to UserHotelAssignment for backward compatibility
        if (user.getHotelId() != null) {
            return user.getHotelId();
        }
        
        return userHotelAssignmentRepository.findOneByUserId(userId)
                .orElseThrow(() ->
                        new ValidationException("Staff user has no hotel assignment")
                )
                .getHotelId();
    }
    
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public void changePassword(Long userId, String currentPassword, String newPassword) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid current password");
        }

        if (!newPassword.matches(STRONG_PASSWORD_REGEX)) {
            throw new InvalidPasswordPolicyException();
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordLastChangedAt(LocalDateTime.now());

        userRepository.save(user);
    }
    
    @Cacheable(value = "users", key = "#userId", unless = "#result == null")
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());
    }

    public List<AdminUserResponse> listAllUsersForAdmin() {

        return userRepository.findAll()
                .stream()
                .map(user -> {

                	Long hotelId = null;

        if (user.getRole() == Role.MANAGER || user.getRole() == Role.RECEPTIONIST) {
            // Prefer hotelId from User entity, fallback to UserHotelAssignment
            hotelId = user.getHotelId();
            if (hotelId == null) {
                hotelId = userHotelAssignmentRepository.findOneByUserId(user.getId())
                        .map(UserHotelAssignment::getHotelId)
                        .orElse(null); // <-- DO NOT THROW
            }
        }

                    return new AdminUserResponse(
                            user.getId(),
                            user.getPublicUserId(),
                            user.getUsername(),
                            user.getFullName(),
                            user.getEmail(),
                            user.getRole(),
                            user.isEnabled(),
                            hotelId
                    );
                })
                .toList();
    }

    @Transactional
    @CacheEvict(value = "users", key = "#targetUserId")
    public void deactivateUser(Long targetUserId, Long adminId) {

        if (targetUserId.equals(adminId)) {
            throw new ValidationException("Admin cannot deactivate self");
        }

        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new UserNotFoundException());

        user.setEnabled(false);
        userRepository.save(user);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public void reassignStaffHotel(Long userId, Long hotelId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());

        if (user.getRole() == Role.GUEST) {
            throw new ValidationException("Guests cannot be assigned to hotels");
        }

        if (user.getRole() != Role.MANAGER && user.getRole() != Role.RECEPTIONIST) {
            throw new InsufficientRoleException("Only staff can be reassigned");
        }

        // Update hotelId in User entity
        user.setHotelId(hotelId);
        userRepository.save(user);
        
        // Also update UserHotelAssignment for backward compatibility
        UserHotelAssignment assignment =
                userHotelAssignmentRepository.findOneByUserId(userId)
                        .orElseThrow(() -> new ValidationException("Staff has no assignment"));

        assignment.setHotelId(hotelId);
        userHotelAssignmentRepository.save(assignment);
    }

    /* ---------------- Helpers ---------------- */

    private void ensureEmailNotExists(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("email", email);
        }
    }
    
    private void ensureUsernameNotExists(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("username", username);
        }
    }
    private String generatePublicUserId(Role role) {
        return role.name() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String generateActivationToken(Long userId) {
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