package com.hotelbooking.hotel.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.hotelbooking.hotel.dto.request.BlockRoomRequest;
import com.hotelbooking.hotel.dto.request.UnblockRoomRequest;
import com.hotelbooking.hotel.dto.response.AvailabilitySearchResponse;
import com.hotelbooking.hotel.exception.AccessDeniedException;
import com.hotelbooking.hotel.service.RoomAvailabilityService;

@ExtendWith(MockitoExtension.class)
class RoomAvailabilityControllerTest {

    @Mock
    private RoomAvailabilityService availabilityService;

    @InjectMocks
    private RoomAvailabilityController availabilityController;

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
    void testBlockRoom_Admin_Success() {
        doNothing().when(availabilityService).blockRoom(any(BlockRoomRequest.class));

        ResponseEntity<?> response = availabilityController.blockRoom("ADMIN", null, blockRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testBlockRoom_Manager_Success() {
        doNothing().when(availabilityService).blockRoom(any(BlockRoomRequest.class));

        ResponseEntity<?> response = availabilityController.blockRoom("MANAGER", 1L, blockRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testBlockRoom_Manager_NoHotelId() {
        assertThrows(AccessDeniedException.class, () -> availabilityController.blockRoom("MANAGER", null, blockRequest));
    }

    @Test
    void testBlockRoom_Manager_WrongHotel() {
        blockRequest.setHotelId(2L);
        assertThrows(AccessDeniedException.class, () -> availabilityController.blockRoom("MANAGER", 1L, blockRequest));
    }

    @Test
    void testBlockRoom_NonAdminOrManager() {
        assertThrows(AccessDeniedException.class, () -> availabilityController.blockRoom("GUEST", null, blockRequest));
    }

    @Test
    void testUnblockRoom_Admin_Success() {
        doNothing().when(availabilityService).unblockRoom(any(UnblockRoomRequest.class));

        ResponseEntity<?> response = availabilityController.unblockRoom("ADMIN", null, unblockRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testUnblockRoom_Manager_WrongHotel() {
        unblockRequest.setHotelId(2L);
        assertThrows(AccessDeniedException.class, () -> availabilityController.unblockRoom("MANAGER", 1L, unblockRequest));
    }

    @Test
    void testSearchAvailability_Success() {
        AvailabilitySearchResponse searchResponse = new AvailabilitySearchResponse(1L, 5, Arrays.asList(1L, 2L, 3L, 4L, 5L));
        when(availabilityService.searchAvailability(1L, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3)))
                .thenReturn(searchResponse);

        ResponseEntity<?> response = availabilityController.searchAvailability(1L, 
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}

