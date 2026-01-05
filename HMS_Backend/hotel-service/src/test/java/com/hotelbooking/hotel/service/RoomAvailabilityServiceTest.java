package com.hotelbooking.hotel.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hotelbooking.hotel.domain.RoomAvailability;
import com.hotelbooking.hotel.dto.request.BlockRoomRequest;
import com.hotelbooking.hotel.dto.request.UnblockRoomRequest;
import com.hotelbooking.hotel.dto.response.AvailabilitySearchResponse;
import com.hotelbooking.hotel.enums.AvailabilityStatus;
import com.hotelbooking.hotel.exception.ValidationException;
import com.hotelbooking.hotel.repository.RoomAvailabilityRepository;
import com.hotelbooking.hotel.service.impl.RoomAvailabilityServiceImpl;

@ExtendWith(MockitoExtension.class)
class RoomAvailabilityServiceTest {

    @Mock
    private RoomAvailabilityRepository availabilityRepository;

    @InjectMocks
    private RoomAvailabilityServiceImpl availabilityService;

    private BlockRoomRequest blockRequest;
    private UnblockRoomRequest unblockRequest;

    @BeforeEach
    void setUp() {
        blockRequest = new BlockRoomRequest();
        blockRequest.setHotelId(1L);
        blockRequest.setRoomId(1L);
        blockRequest.setFromDate(LocalDate.now().plusDays(1));
        blockRequest.setToDate(LocalDate.now().plusDays(3));
        blockRequest.setReason("Maintenance");

        unblockRequest = new UnblockRoomRequest();
        unblockRequest.setHotelId(1L);
        unblockRequest.setRoomId(1L);
        unblockRequest.setFromDate(LocalDate.now().plusDays(1));
        unblockRequest.setToDate(LocalDate.now().plusDays(3));
    }

    @Test
    void testBlockRoom_Success() {
        when(availabilityRepository.findByRoomIdAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(availabilityRepository.save(any(RoomAvailability.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        availabilityService.blockRoom(blockRequest);

        // blockRequest covers 3 days (fromDate to toDate inclusive), so save should be called 3 times
        verify(availabilityRepository, org.mockito.Mockito.times(3)).save(any(RoomAvailability.class));
    }

    @Test
    void testBlockRoom_ExistingAvailability() {
        RoomAvailability existing = RoomAvailability.builder()
                .roomId(1L)
                .date(LocalDate.now().plusDays(1))
                .status(AvailabilityStatus.AVAILABLE)
                .build();

        when(availabilityRepository.findByRoomIdAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.of(existing));
        when(availabilityRepository.save(any(RoomAvailability.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        availabilityService.blockRoom(blockRequest);

        // blockRequest covers 3 days (fromDate to toDate inclusive), so save should be called 3 times
        verify(availabilityRepository, org.mockito.Mockito.times(3)).save(any(RoomAvailability.class));
    }

    @Test
    void testBlockRoom_InvalidDateRange() {
        blockRequest.setFromDate(LocalDate.now().plusDays(3));
        blockRequest.setToDate(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> availabilityService.blockRoom(blockRequest));
    }

    @Test
    void testUnblockRoom_Success() {
        RoomAvailability blocked = RoomAvailability.builder()
                .roomId(1L)
                .date(LocalDate.now().plusDays(1))
                .status(AvailabilityStatus.BLOCKED)
                .build();

        when(availabilityRepository.findByRoomIdAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.of(blocked));
        when(availabilityRepository.save(any(RoomAvailability.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        availabilityService.unblockRoom(unblockRequest);

        verify(availabilityRepository).save(any(RoomAvailability.class));
    }

    @Test
    void testUnblockRoom_ReservedNotUnblocked() {
        RoomAvailability reserved = RoomAvailability.builder()
                .roomId(1L)
                .date(LocalDate.now().plusDays(1))
                .status(AvailabilityStatus.RESERVED)
                .build();

        when(availabilityRepository.findByRoomIdAndDate(anyLong(), any(LocalDate.class)))
                .thenReturn(Optional.of(reserved));

        availabilityService.unblockRoom(unblockRequest);

        verify(availabilityRepository, org.mockito.Mockito.never()).save(any(RoomAvailability.class));
    }

    @Test
    void testUnblockRoom_InvalidDateRange() {
        unblockRequest.setFromDate(LocalDate.now().plusDays(3));
        unblockRequest.setToDate(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> availabilityService.unblockRoom(unblockRequest));
    }

    @Test
    void testSearchAvailability_Success() {
        when(availabilityRepository.findAvailableRoomIdsStrict(1L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(2)))
                .thenReturn(Arrays.asList(1L, 2L, 3L));

        AvailabilitySearchResponse response = availabilityService.searchAvailability(
                1L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));

        assertEquals(3, response.getAvailableRooms());
        assertEquals(3, response.getAvailableRoomIds().size());
    }

    @Test
    void testSearchAvailability_NullDates() {
        assertThrows(ValidationException.class, () -> availabilityService.searchAvailability(1L, null, null));
    }

    @Test
    void testSearchAvailability_CheckInAfterCheckOut() {
        assertThrows(ValidationException.class, () -> availabilityService.searchAvailability(
                1L, LocalDate.now().plusDays(3), LocalDate.now().plusDays(1)));
    }

    @Test
    void testSearchAvailability_CheckInEqualsCheckOut() {
        LocalDate date = LocalDate.now().plusDays(1);
        assertThrows(ValidationException.class, () -> availabilityService.searchAvailability(1L, date, date));
    }

    @Test
    void testSearchAvailability_PastCheckIn() {
        assertThrows(ValidationException.class, () -> availabilityService.searchAvailability(
                1L, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1)));
    }
}

