package com.hotelbooking.auth.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

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

import com.hotelbooking.auth.domain.Role;
import com.hotelbooking.auth.domain.User;
import com.hotelbooking.auth.dto.AdminUserResponse;
import com.hotelbooking.auth.dto.StaffCreateRequest;
import com.hotelbooking.auth.dto.StaffUpdateRequest;
import com.hotelbooking.auth.service.UserService;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminController adminController;

    private User testStaff;

    @BeforeEach
    void setUp() {
        testStaff = User.builder()
                .id(1L)
                .publicUserId("MANAGER-XYZ789")
                .username("manager1")
                .fullName("Manager One")
                .email("manager@example.com")
                .role(Role.MANAGER)
                .hotelId(100L)
                .enabled(false)
                .build();
    }

    @Test
    void testCreateStaff_Success() {
        StaffCreateRequest request = new StaffCreateRequest();
        request.setFullName("New Manager");
        request.setUsername("newmanager");
        request.setEmail("newmanager@example.com");
        request.setPassword("Password123!");
        request.setRole("MANAGER");
        request.setHotelId(100L);

        when(userService.createStaffUser(any(User.class), anyLong())).thenReturn("activation-token-123");

        ResponseEntity<Map<String, String>> response = adminController.createStaff(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("activation-token-123", response.getBody().get("activationToken"));
    }

    @Test
    void testListUsers_Success() {
        AdminUserResponse user1 = new AdminUserResponse(1L, "GUEST-ABC", "user1", "User One", "user1@example.com", Role.GUEST, true, null);
        AdminUserResponse user2 = new AdminUserResponse(2L, "MANAGER-XYZ", "manager1", "Manager One", "manager@example.com", Role.MANAGER, false, 100L);

        when(userService.listAllUsersForAdmin()).thenReturn(Arrays.asList(user1, user2));

        ResponseEntity<List<AdminUserResponse>> response = adminController.listUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void testActivateUser_Success() {
        ResponseEntity<Void> response = adminController.activateUser(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testDeactivateUser_Success() {
        ResponseEntity<Void> response = adminController.deactivateUser(1L, 999L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testUpdateStaff_Success() {
        StaffUpdateRequest request = new StaffUpdateRequest();
        request.setFullName("Updated Manager");
        request.setUsername("updatedmanager");
        request.setEmail("updated@example.com");
        request.setHotelId(200L);

        User updatedUser = User.builder()
                .id(1L)
                .publicUserId("MANAGER-XYZ789")
                .username("updatedmanager")
                .fullName("Updated Manager")
                .email("updated@example.com")
                .role(Role.MANAGER)
                .hotelId(200L)
                .enabled(true)
                .build();

        when(userService.updateStaff(anyLong(), anyString(), anyString(), anyString(), anyLong())).thenReturn(updatedUser);

        ResponseEntity<AdminUserResponse> response = adminController.updateStaff(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated Manager", response.getBody().getFullName());
    }

    @Test
    void testGetUsersByHotelId_Success() {
        AdminUserResponse user1 = new AdminUserResponse(1L, "MANAGER-ABC", "manager1", "Manager One", "m1@example.com", Role.MANAGER, true, 100L);
        AdminUserResponse user2 = new AdminUserResponse(2L, "RECEPTIONIST-XYZ", "recep1", "Receptionist One", "r1@example.com", Role.RECEPTIONIST, true, 100L);

        when(userService.getUsersByHotelId(100L)).thenReturn(Arrays.asList(user1, user2));

        ResponseEntity<List<AdminUserResponse>> response = adminController.getUsersByHotelId(100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void testReassignHotel_Success() {
        ResponseEntity<Void> response = adminController.reassignHotel(1L, 200L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testCreateStaff_InvalidRole() {
        StaffCreateRequest request = new StaffCreateRequest();
        request.setRole("INVALID_ROLE");

        assertThrows(com.hotelbooking.auth.exception.ValidationException.class, () -> adminController.createStaff(request));
    }

    @Test
    void testCreateStaff_GuestRole() {
        StaffCreateRequest request = new StaffCreateRequest();
        request.setRole("GUEST");

        assertThrows(com.hotelbooking.auth.exception.InsufficientRoleException.class, () -> adminController.createStaff(request));
    }

    @Test
    void testCreateStaff_AdminRole() {
        StaffCreateRequest request = new StaffCreateRequest();
        request.setRole("ADMIN");

        assertThrows(com.hotelbooking.auth.exception.InsufficientRoleException.class, () -> adminController.createStaff(request));
    }

    @Test
    void testUpdateStaff_NoHotelId() {
        StaffUpdateRequest request = new StaffUpdateRequest();
        request.setFullName("Updated Manager");
        request.setUsername("updatedmanager");
        request.setEmail("updated@example.com");
        request.setHotelId(null);

        User updatedUser = User.builder()
                .id(1L)
                .publicUserId("MANAGER-XYZ789")
                .username("updatedmanager")
                .fullName("Updated Manager")
                .email("updated@example.com")
                .role(Role.MANAGER)
                .hotelId(null)
                .enabled(true)
                .build();

        when(userService.updateStaff(anyLong(), anyString(), anyString(), anyString(), any())).thenReturn(updatedUser);
        when(userService.getAssignedHotelId(anyLong())).thenReturn(100L);

        ResponseEntity<AdminUserResponse> response = adminController.updateStaff(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}

