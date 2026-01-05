package com.hotelbooking.auth.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hotelbooking.auth.domain.UserHotelAssignment;

@ExtendWith(MockitoExtension.class)
class UserHotelAssignmentRepositoryTest {

    @Mock
    private UserHotelAssignmentRepository userHotelAssignmentRepository;

    private UserHotelAssignment testAssignment;

    @BeforeEach
    void setUp() {
        testAssignment = UserHotelAssignment.builder()
                .id(1L)
                .userId(1L)
                .hotelId(100L)
                .build();
    }

    @Test
    void testFindOneByUserId_Success() {
        when(userHotelAssignmentRepository.findOneByUserId(1L)).thenReturn(Optional.of(testAssignment));

        Optional<UserHotelAssignment> found = userHotelAssignmentRepository.findOneByUserId(1L);

        assertTrue(found.isPresent());
        assertEquals(100L, found.get().getHotelId());
    }

    @Test
    void testFindOneByUserId_NotFound() {
        when(userHotelAssignmentRepository.findOneByUserId(999L)).thenReturn(Optional.empty());

        Optional<UserHotelAssignment> found = userHotelAssignmentRepository.findOneByUserId(999L);

        assertFalse(found.isPresent());
    }

    @Test
    void testExistsByUserId_True() {
        when(userHotelAssignmentRepository.existsByUserId(1L)).thenReturn(true);

        boolean exists = userHotelAssignmentRepository.existsByUserId(1L);

        assertTrue(exists);
    }

    @Test
    void testExistsByUserId_False() {
        when(userHotelAssignmentRepository.existsByUserId(999L)).thenReturn(false);

        boolean exists = userHotelAssignmentRepository.existsByUserId(999L);

        assertFalse(exists);
    }
}

