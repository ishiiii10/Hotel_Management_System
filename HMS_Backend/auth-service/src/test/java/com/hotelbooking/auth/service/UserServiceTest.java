package com.hotelbooking.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hotelbooking.auth.domain.ActivationToken;
import com.hotelbooking.auth.domain.Role;
import com.hotelbooking.auth.domain.User;
import com.hotelbooking.auth.domain.UserHotelAssignment;
import com.hotelbooking.auth.exception.AccountDisabledException;
import com.hotelbooking.auth.exception.CredentialsExpiredException;
import com.hotelbooking.auth.exception.InsufficientRoleException;
import com.hotelbooking.auth.exception.InvalidCredentialsException;
import com.hotelbooking.auth.exception.InvalidPasswordPolicyException;
import com.hotelbooking.auth.exception.UserAlreadyExistsException;
import com.hotelbooking.auth.exception.UserNotFoundException;
import com.hotelbooking.auth.exception.ValidationException;
import com.hotelbooking.auth.repository.ActivationTokenRepository;
import com.hotelbooking.auth.repository.UserHotelAssignmentRepository;
import com.hotelbooking.auth.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserHotelAssignmentRepository userHotelAssignmentRepository;

    @Mock
    private ActivationTokenRepository activationTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .publicUserId("GUEST-ABC123")
                .username("testuser")
                .fullName("Test User")
                .email("test@example.com")
                .password("encodedPassword")
                .role(Role.GUEST)
                .enabled(true)
                .passwordLastChangedAt(LocalDateTime.now().minusDays(30))
                .build();
    }

    @Test
    void testRegisterGuest_Success() {
        User newUser = User.builder()
                .username("newuser")
                .email("newuser@example.com")
                .password("Password123!")
                .role(Role.GUEST)
                .build();

        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.registerGuest(newUser);

        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegisterGuest_EmailExists() {
        User newUser = User.builder()
                .email("test@example.com")
                .role(Role.GUEST)
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        assertThrows(UserAlreadyExistsException.class, () -> userService.registerGuest(newUser));
    }

    @Test
    void testAuthenticate_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        User result = userService.authenticate("test@example.com", "password123");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void testAuthenticate_UserNotFound() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> userService.authenticate("notfound@example.com", "password"));
    }

    @Test
    void testAuthenticate_WrongPassword() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> userService.authenticate("test@example.com", "wrongpassword"));
    }

    @Test
    void testGetUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void testGetUserById_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(999L));
    }

    @Test
    void testCreateStaffUser_Success() {
        User staff = User.builder()
                .username("manager1")
                .email("manager@example.com")
                .password("Password123!")
                .role(Role.MANAGER)
                .build();

        when(userRepository.findByEmail("manager@example.com")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("manager1")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(activationTokenRepository.save(any(ActivationToken.class))).thenReturn(new ActivationToken());

        String token = userService.createStaffUser(staff, 100L);

        assertNotNull(token);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testCreateStaffUser_MissingHotelId() {
        User staff = User.builder()
                .role(Role.MANAGER)
                .build();

        assertThrows(com.hotelbooking.auth.exception.MissingRequiredFieldException.class, 
                () -> userService.createStaffUser(staff, null));
    }

    @Test
    void testActivateUser_Success() {
        ActivationToken token = ActivationToken.builder()
                .token("test-token")
                .userId(1L)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();

        User staff = User.builder()
                .id(1L)
                .role(Role.MANAGER)
                .enabled(false)
                .build();

        UserHotelAssignment assignment = UserHotelAssignment.builder()
                .userId(1L)
                .hotelId(100L)
                .build();

        when(activationTokenRepository.findByToken("test-token")).thenReturn(Optional.of(token));
        when(userRepository.findById(1L)).thenReturn(Optional.of(staff));
        when(userHotelAssignmentRepository.findOneByUserId(1L)).thenReturn(Optional.of(assignment));
        when(userRepository.save(any(User.class))).thenReturn(staff);
        when(activationTokenRepository.save(any(ActivationToken.class))).thenReturn(token);

        userService.activateUser("test-token");

        verify(userRepository).save(any(User.class));
        verify(activationTokenRepository).save(any(ActivationToken.class));
    }

    @Test
    void testActivateUser_InvalidToken() {
        when(activationTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> userService.activateUser("invalid-token"));
    }

    @Test
    void testChangePassword_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldpass", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("NewPass123!")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.changePassword(1L, "oldpass", "NewPass123!");

        verify(userRepository).save(any(User.class));
    }

    @Test
    void testChangePassword_WrongCurrentPassword() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpass", "encodedPassword")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> userService.changePassword(1L, "wrongpass", "NewPass123!"));
    }

    @Test
    void testListAllUsersForAdmin_Success() {
        User user1 = User.builder().id(1L).role(Role.GUEST).build();
        User user2 = User.builder().id(2L).role(Role.MANAGER).hotelId(100L).build();

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        List<com.hotelbooking.auth.dto.AdminUserResponse> result = userService.listAllUsersForAdmin();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testGetUsersByHotelId_Success() {
        User user1 = User.builder().id(1L).role(Role.MANAGER).hotelId(100L).build();
        User user2 = User.builder().id(2L).role(Role.RECEPTIONIST).hotelId(100L).build();

        when(userRepository.findByHotelId(100L)).thenReturn(Arrays.asList(user1, user2));

        List<com.hotelbooking.auth.dto.AdminUserResponse> result = userService.getUsersByHotelId(100L);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testDeactivateUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.deactivateUser(1L, 999L);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void testDeactivateUser_CannotDeactivateSelf() {
        assertThrows(ValidationException.class, () -> userService.deactivateUser(1L, 1L));
    }

    @Test
    void testActivateUserById_Success() {
        User disabledUser = User.builder().id(1L).enabled(false).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(disabledUser));
        when(userRepository.save(any(User.class))).thenReturn(disabledUser);

        userService.activateUser(1L);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void testActivateUserById_AlreadyActivated() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertThrows(ValidationException.class, () -> userService.activateUser(1L));
    }

    @Test
    void testGetAssignedHotelId_FromUserEntity() {
        User manager = User.builder().id(1L).hotelId(100L).role(Role.MANAGER).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(manager));

        Long hotelId = userService.getAssignedHotelId(1L);

        assertEquals(100L, hotelId);
        verify(userHotelAssignmentRepository, never()).findOneByUserId(anyLong());
    }

    @Test
    void testGetAssignedHotelId_FromAssignment() {
        User manager = User.builder().id(1L).hotelId(null).role(Role.MANAGER).build();
        UserHotelAssignment assignment = UserHotelAssignment.builder().userId(1L).hotelId(200L).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(manager));
        when(userHotelAssignmentRepository.findOneByUserId(1L)).thenReturn(Optional.of(assignment));

        Long hotelId = userService.getAssignedHotelId(1L);

        assertEquals(200L, hotelId);
    }

    @Test
    void testGetAssignedHotelId_NoAssignment() {
        User manager = User.builder().id(1L).hotelId(null).role(Role.MANAGER).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(manager));
        when(userHotelAssignmentRepository.findOneByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> userService.getAssignedHotelId(1L));
    }

    @Test
    void testAuthenticate_AccountDisabled() {
        User disabledUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .enabled(false)
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(disabledUser));

        assertThrows(AccountDisabledException.class, () -> userService.authenticate("test@example.com", "password"));
    }

    @Test
    void testAuthenticate_PasswordExpired() {
        User expiredUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .enabled(true)
                .passwordLastChangedAt(LocalDateTime.now().minusDays(100))
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(expiredUser));

        assertThrows(CredentialsExpiredException.class, () -> userService.authenticate("test@example.com", "password"));
    }

    @Test
    void testRegisterGuest_InvalidRole() {
        User admin = User.builder()
                .email("admin@example.com")
                .role(Role.ADMIN)
                .build();

        assertThrows(InsufficientRoleException.class, () -> userService.registerGuest(admin));
    }

    @Test
    void testCreateStaffUser_InvalidRole() {
        User guest = User.builder()
                .role(Role.GUEST)
                .build();

        assertThrows(InsufficientRoleException.class, () -> userService.createStaffUser(guest, 100L));
    }

    @Test
    void testCreateStaffUser_AdminRole() {
        User admin = User.builder()
                .role(Role.ADMIN)
                .build();

        assertThrows(InsufficientRoleException.class, () -> userService.createStaffUser(admin, 100L));
    }

    @Test
    void testActivateUser_TokenExpired() {
        ActivationToken token = ActivationToken.builder()
                .token("expired-token")
                .userId(1L)
                .expiresAt(LocalDateTime.now().minusHours(1))
                .used(false)
                .build();

        when(activationTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(token));

        assertThrows(CredentialsExpiredException.class, () -> userService.activateUser("expired-token"));
    }

    @Test
    void testActivateUser_TokenAlreadyUsed() {
        ActivationToken token = ActivationToken.builder()
                .token("used-token")
                .userId(1L)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(true)
                .build();

        when(activationTokenRepository.findByToken("used-token")).thenReturn(Optional.of(token));

        assertThrows(ValidationException.class, () -> userService.activateUser("used-token"));
    }

    @Test
    void testActivateUser_UserAlreadyEnabled() {
        ActivationToken token = ActivationToken.builder()
                .token("test-token")
                .userId(1L)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();

        User enabledUser = User.builder()
                .id(1L)
                .role(Role.MANAGER)
                .enabled(true)
                .build();

        when(activationTokenRepository.findByToken("test-token")).thenReturn(Optional.of(token));
        when(userRepository.findById(1L)).thenReturn(Optional.of(enabledUser));

        assertThrows(ValidationException.class, () -> userService.activateUser("test-token"));
    }

    @Test
    void testActivateUser_InvalidRole() {
        ActivationToken token = ActivationToken.builder()
                .token("test-token")
                .userId(1L)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();

        User guest = User.builder()
                .id(1L)
                .role(Role.GUEST)
                .enabled(false)
                .build();

        when(activationTokenRepository.findByToken("test-token")).thenReturn(Optional.of(token));
        when(userRepository.findById(1L)).thenReturn(Optional.of(guest));

        assertThrows(ValidationException.class, () -> userService.activateUser("test-token"));
    }

    @Test
    void testActivateUser_NoHotelAssignment() {
        ActivationToken token = ActivationToken.builder()
                .token("test-token")
                .userId(1L)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();

        User staff = User.builder()
                .id(1L)
                .role(Role.MANAGER)
                .enabled(false)
                .build();

        when(activationTokenRepository.findByToken("test-token")).thenReturn(Optional.of(token));
        when(userRepository.findById(1L)).thenReturn(Optional.of(staff));
        when(userHotelAssignmentRepository.findOneByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> userService.activateUser("test-token"));
    }

    @Test
    void testChangePassword_WeakPassword() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldpass", "encodedPassword")).thenReturn(true);

        assertThrows(InvalidPasswordPolicyException.class, () -> userService.changePassword(1L, "oldpass", "weak"));
    }

    @Test
    void testReassignStaffHotel_GuestRole() {
        User guest = User.builder().id(1L).role(Role.GUEST).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(guest));

        assertThrows(ValidationException.class, () -> userService.reassignStaffHotel(1L, 100L));
    }

    @Test
    void testReassignStaffHotel_AdminRole() {
        User admin = User.builder().id(1L).role(Role.ADMIN).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThrows(InsufficientRoleException.class, () -> userService.reassignStaffHotel(1L, 100L));
    }

    @Test
    void testReassignStaffHotel_NoAssignment() {
        User manager = User.builder().id(1L).role(Role.MANAGER).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(manager));
        when(userHotelAssignmentRepository.findOneByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> userService.reassignStaffHotel(1L, 100L));
    }

    @Test
    void testUpdateStaff_InvalidRole() {
        User guest = User.builder().id(1L).role(Role.GUEST).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(guest));

        assertThrows(ValidationException.class, () -> userService.updateStaff(1L, "Name", "user", "email@test.com", null));
    }

    @Test
    void testUpdateStaff_EmailExists() {
        User staff = User.builder().id(1L).email("old@example.com").role(Role.MANAGER).build();
        User existingUser = User.builder().id(2L).email("new@example.com").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(staff));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.of(existingUser));

        assertThrows(UserAlreadyExistsException.class, () -> userService.updateStaff(1L, "Name", "user", "new@example.com", null));
    }

    @Test
    void testUpdateStaff_UsernameExists() {
        User staff = User.builder().id(1L).email("old@test.com").username("olduser").role(Role.MANAGER).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(staff));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.of(User.builder().id(2L).build()));

        assertThrows(UserAlreadyExistsException.class, () -> userService.updateStaff(1L, "Name", "newuser", "email@test.com", null));
    }

    @Test
    void testUpdateStaff_WithHotelId_AssignmentExists() {
        User staff = User.builder().id(1L).email("old@test.com").username("olduser").role(Role.MANAGER).build();
        UserHotelAssignment assignment = UserHotelAssignment.builder().userId(1L).hotelId(100L).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(staff));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userHotelAssignmentRepository.findOneByUserId(1L)).thenReturn(Optional.of(assignment));
        when(userRepository.save(any(User.class))).thenReturn(staff);
        when(userHotelAssignmentRepository.save(any(UserHotelAssignment.class))).thenReturn(assignment);

        User result = userService.updateStaff(1L, "Name", "user", "email@test.com", 200L);

        assertNotNull(result);
        verify(userHotelAssignmentRepository).save(any(UserHotelAssignment.class));
    }

    @Test
    void testUpdateStaff_WithHotelId_NoAssignment() {
        User staff = User.builder().id(1L).email("old@test.com").username("olduser").role(Role.MANAGER).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(staff));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userHotelAssignmentRepository.findOneByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(staff);
        when(userHotelAssignmentRepository.save(any(UserHotelAssignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateStaff(1L, "Name", "user", "email@test.com", 200L);

        assertNotNull(result);
        verify(userHotelAssignmentRepository).save(any(UserHotelAssignment.class));
    }

    @Test
    void testUpdateStaff_RemoveHotelId() {
        User staff = User.builder().id(1L).email("old@test.com").username("olduser").role(Role.MANAGER).hotelId(100L).build();
        UserHotelAssignment assignment = UserHotelAssignment.builder().userId(1L).hotelId(100L).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(staff));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userHotelAssignmentRepository.findOneByUserId(1L)).thenReturn(Optional.of(assignment));
        when(userRepository.save(any(User.class))).thenReturn(staff);

        User result = userService.updateStaff(1L, "Name", "user", "email@test.com", null);

        assertNotNull(result);
        verify(userHotelAssignmentRepository).delete(any(UserHotelAssignment.class));
    }

    @Test
    void testListAllUsersForAdmin_WithHotelAssignment() {
        User manager1 = User.builder().id(1L).role(Role.MANAGER).hotelId(null).build();
        User manager2 = User.builder().id(2L).role(Role.MANAGER).hotelId(200L).build();
        UserHotelAssignment assignment = UserHotelAssignment.builder().userId(1L).hotelId(100L).build();

        when(userRepository.findAll()).thenReturn(Arrays.asList(manager1, manager2));
        when(userHotelAssignmentRepository.findOneByUserId(1L)).thenReturn(Optional.of(assignment));

        List<com.hotelbooking.auth.dto.AdminUserResponse> result = userService.listAllUsersForAdmin();

        assertNotNull(result);
        assertEquals(2, result.size());
    }
}

