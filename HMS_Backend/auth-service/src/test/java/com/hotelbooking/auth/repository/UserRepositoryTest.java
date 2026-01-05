package com.hotelbooking.auth.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hotelbooking.auth.domain.Role;
import com.hotelbooking.auth.domain.User;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @Mock
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .fullName("Test User")
                .email("test@example.com")
                .password("encodedPassword")
                .role(Role.GUEST)
                .enabled(true)
                .build();
    }

    @Test
    void testFindByEmail_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        Optional<User> found = userRepository.findByEmail("test@example.com");

        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void testFindByEmail_NotFound() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        Optional<User> found = userRepository.findByEmail("notfound@example.com");

        assertFalse(found.isPresent());
    }

    @Test
    void testFindByUsername_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        Optional<User> found = userRepository.findByUsername("testuser");

        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
    }

    @Test
    void testExistsByUsername_True() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        boolean exists = userRepository.existsByUsername("testuser");

        assertTrue(exists);
    }

    @Test
    void testExistsByUsername_False() {
        when(userRepository.existsByUsername("nonexistent")).thenReturn(false);

        boolean exists = userRepository.existsByUsername("nonexistent");

        assertFalse(exists);
    }

    @Test
    void testFindByHotelId_Success() {
        User manager1 = User.builder().id(1L).username("manager1").role(Role.MANAGER).hotelId(100L).build();
        User manager2 = User.builder().id(2L).username("manager2").role(Role.MANAGER).hotelId(100L).build();

        when(userRepository.findByHotelId(100L)).thenReturn(Arrays.asList(manager1, manager2));

        List<User> users = userRepository.findByHotelId(100L);

        assertNotNull(users);
        assertEquals(2, users.size());
    }

    @Test
    void testFindByHotelId_Empty() {
        when(userRepository.findByHotelId(999L)).thenReturn(Arrays.asList());

        List<User> users = userRepository.findByHotelId(999L);

        assertNotNull(users);
        assertEquals(0, users.size());
    }
}

