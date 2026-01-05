package com.hotelbooking.hotel.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.hotelbooking.hotel.dto.request.CreateRoomRequest;
import com.hotelbooking.hotel.dto.response.RoomResponse;
import com.hotelbooking.hotel.enums.RoomCategory;
import com.hotelbooking.hotel.enums.RoomStatus;
import com.hotelbooking.hotel.exception.AccessDeniedException;
import com.hotelbooking.hotel.service.RoomService;

@ExtendWith(MockitoExtension.class)
class RoomControllerTest {

    @Mock
    private RoomService roomService;

    @InjectMocks
    private RoomController roomController;

    private CreateRoomRequest createRoomRequest;
    private RoomResponse roomResponse;

    @BeforeEach
    void setUp() {
        createRoomRequest = new CreateRoomRequest();
        createRoomRequest.setHotelId(1L);
        createRoomRequest.setRoomNumber("101");
        createRoomRequest.setRoomType(RoomCategory.STANDARD);
        createRoomRequest.setPricePerNight(BigDecimal.valueOf(1000));
        createRoomRequest.setMaxOccupancy(2);
        createRoomRequest.setStatus(RoomStatus.AVAILABLE);
        createRoomRequest.setIsActive(true);

        roomResponse = new RoomResponse(1L, 1L, "101", RoomCategory.STANDARD, 
                BigDecimal.valueOf(1000), 2, 1, "Double", 200, "AC, TV", "Nice room", 
                RoomStatus.AVAILABLE, true);
    }

    @Test
    void testCreateRoom_Admin_Success() {
        when(roomService.createRoom(any(CreateRoomRequest.class))).thenReturn(1L);

        ResponseEntity<?> response = roomController.createRoom("ADMIN", null, createRoomRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testCreateRoom_Manager_Success() {
        when(roomService.createRoom(any(CreateRoomRequest.class))).thenReturn(1L);

        ResponseEntity<?> response = roomController.createRoom("MANAGER", 1L, createRoomRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testCreateRoom_Manager_NoHotelId() {
        assertThrows(AccessDeniedException.class, () -> roomController.createRoom("MANAGER", null, createRoomRequest));
    }

    @Test
    void testCreateRoom_Manager_WrongHotel() {
        createRoomRequest.setHotelId(2L);
        assertThrows(AccessDeniedException.class, () -> roomController.createRoom("MANAGER", 1L, createRoomRequest));
    }

    @Test
    void testCreateRoom_NonAdminOrManager() {
        assertThrows(AccessDeniedException.class, () -> roomController.createRoom("GUEST", null, createRoomRequest));
    }

    @Test
    void testGetRoomsByHotel_Success() {
        when(roomService.getRoomsByHotel(1L)).thenReturn(Arrays.asList(roomResponse));

        ResponseEntity<?> response = roomController.getRoomsByHotel(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetRoomById_Success() {
        when(roomService.getRoomById(1L)).thenReturn(roomResponse);

        ResponseEntity<?> response = roomController.getRoomById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testDeleteRoom_Admin_Success() {
        when(roomService.getRoomById(1L)).thenReturn(roomResponse);

        ResponseEntity<?> response = roomController.deleteRoom("ADMIN", null, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testDeleteRoom_Manager_Success() {
        when(roomService.getRoomById(1L)).thenReturn(roomResponse);

        ResponseEntity<?> response = roomController.deleteRoom("MANAGER", 1L, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testDeleteRoom_Manager_WrongHotel() {
        RoomResponse otherHotelRoom = new RoomResponse(2L, 2L, "201", RoomCategory.STANDARD, 
                BigDecimal.valueOf(1000), 2, 1, "Double", 200, "AC", "Room", 
                RoomStatus.AVAILABLE, true);
        when(roomService.getRoomById(2L)).thenReturn(otherHotelRoom);

        assertThrows(AccessDeniedException.class, () -> roomController.deleteRoom("MANAGER", 1L, 2L));
    }

    @Test
    void testUpdateRoom_Admin_Success() {
        when(roomService.getRoomById(1L)).thenReturn(roomResponse);
        when(roomService.updateRoom(anyLong(), any(CreateRoomRequest.class))).thenReturn(1L);

        ResponseEntity<?> response = roomController.updateRoom("ADMIN", null, 1L, createRoomRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testUpdateRoom_Manager_WrongHotel() {
        RoomResponse otherHotelRoom = new RoomResponse(2L, 2L, "201", RoomCategory.STANDARD, 
                BigDecimal.valueOf(1000), 2, 1, "Double", 200, "AC", "Room", 
                RoomStatus.AVAILABLE, true);
        when(roomService.getRoomById(2L)).thenReturn(otherHotelRoom);

        assertThrows(AccessDeniedException.class, () -> roomController.updateRoom("MANAGER", 1L, 2L, createRoomRequest));
    }

    @Test
    void testUpdateRoomStatus_Admin_Success() {
        when(roomService.getRoomById(1L)).thenReturn(roomResponse);

        ResponseEntity<?> response = roomController.updateRoomStatus("ADMIN", null, 1L, RoomStatus.MAINTENANCE);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testUpdateRoomStatus_Receptionist_Success() {
        when(roomService.getRoomById(1L)).thenReturn(roomResponse);

        ResponseEntity<?> response = roomController.updateRoomStatus("RECEPTIONIST", 1L, 1L, RoomStatus.AVAILABLE);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testUpdateRoomStatus_NonAuthorized() {
        assertThrows(AccessDeniedException.class, () -> roomController.updateRoomStatus("GUEST", null, 1L, RoomStatus.AVAILABLE));
    }

    @Test
    void testUpdateRoomActiveStatus_Admin_Success() {
        when(roomService.getRoomById(1L)).thenReturn(roomResponse);

        ResponseEntity<?> response = roomController.updateRoomActiveStatus("ADMIN", null, 1L, true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testUpdateRoomActiveStatus_Manager_WrongHotel() {
        RoomResponse otherHotelRoom = new RoomResponse(2L, 2L, "201", RoomCategory.STANDARD, 
                BigDecimal.valueOf(1000), 2, 1, "Double", 200, "AC", "Room", 
                RoomStatus.AVAILABLE, true);
        when(roomService.getRoomById(2L)).thenReturn(otherHotelRoom);

        assertThrows(AccessDeniedException.class, () -> roomController.updateRoomActiveStatus("MANAGER", 1L, 2L, true));
    }
}

