package com.hotelbooking.hotel.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hotelbooking.hotel.domain.RoomAvailability;
import com.hotelbooking.hotel.enums.AvailabilityStatus;

@ExtendWith(MockitoExtension.class)
class RoomAvailabilityRepositoryTest {

    @Mock
    private RoomAvailabilityRepository availabilityRepository;

    private RoomAvailability testAvailability;

    @BeforeEach
    void setUp() {
        testAvailability = RoomAvailability.builder()
                .id(1L)
                .hotelId(1L)
                .roomId(1L)
                .date(LocalDate.now().plusDays(1))
                .status(AvailabilityStatus.AVAILABLE)
                .build();
    }

    @Test
    void testFindByRoomIdAndDate_Success() {
        when(availabilityRepository.findByRoomIdAndDate(1L, LocalDate.now().plusDays(1)))
                .thenReturn(Optional.of(testAvailability));

        Optional<RoomAvailability> availability = availabilityRepository.findByRoomIdAndDate(1L, LocalDate.now().plusDays(1));

        assertTrue(availability.isPresent());
        assertEquals(AvailabilityStatus.AVAILABLE, availability.get().getStatus());
    }

    @Test
    void testFindByRoomIdAndDate_NotFound() {
        when(availabilityRepository.findByRoomIdAndDate(999L, LocalDate.now().plusDays(1)))
                .thenReturn(Optional.empty());

        Optional<RoomAvailability> availability = availabilityRepository.findByRoomIdAndDate(999L, LocalDate.now().plusDays(1));

        assertFalse(availability.isPresent());
    }

    @Test
    void testExistsByRoomIdAndDate_True() {
        when(availabilityRepository.existsByRoomIdAndDate(1L, LocalDate.now().plusDays(1))).thenReturn(true);

        boolean exists = availabilityRepository.existsByRoomIdAndDate(1L, LocalDate.now().plusDays(1));

        assertTrue(exists);
    }

    @Test
    void testExistsByRoomIdAndDate_False() {
        when(availabilityRepository.existsByRoomIdAndDate(999L, LocalDate.now().plusDays(1))).thenReturn(false);

        boolean exists = availabilityRepository.existsByRoomIdAndDate(999L, LocalDate.now().plusDays(1));

        assertFalse(exists);
    }

    @Test
    void testFindByRoomIdAndDateBetween_Success() {
        RoomAvailability avail1 = RoomAvailability.builder().id(1L).roomId(1L).date(LocalDate.now().plusDays(1)).build();
        RoomAvailability avail2 = RoomAvailability.builder().id(2L).roomId(1L).date(LocalDate.now().plusDays(2)).build();

        when(availabilityRepository.findByRoomIdAndDateBetween(1L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3)))
                .thenReturn(Arrays.asList(avail1, avail2));

        List<RoomAvailability> availabilities = availabilityRepository.findByRoomIdAndDateBetween(
                1L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));

        assertNotNull(availabilities);
        assertEquals(2, availabilities.size());
    }

    @Test
    void testFindAvailableRoomIdsStrict_Success() {
        when(availabilityRepository.findAvailableRoomIdsStrict(1L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3)))
                .thenReturn(Arrays.asList(1L, 2L, 3L));

        List<Long> roomIds = availabilityRepository.findAvailableRoomIdsStrict(
                1L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));

        assertNotNull(roomIds);
        assertEquals(3, roomIds.size());
    }

    @Test
    void testFindByHotelIdAndDateBetweenAndStatus_Success() {
        RoomAvailability avail1 = RoomAvailability.builder()
                .id(1L).hotelId(1L).date(LocalDate.now().plusDays(1))
                .status(AvailabilityStatus.AVAILABLE).build();

        when(availabilityRepository.findByHotelIdAndDateBetweenAndStatus(1L, 
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), AvailabilityStatus.AVAILABLE))
                .thenReturn(Arrays.asList(avail1));

        List<RoomAvailability> availabilities = availabilityRepository.findByHotelIdAndDateBetweenAndStatus(
                1L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), AvailabilityStatus.AVAILABLE);

        assertNotNull(availabilities);
        assertEquals(1, availabilities.size());
    }
}

