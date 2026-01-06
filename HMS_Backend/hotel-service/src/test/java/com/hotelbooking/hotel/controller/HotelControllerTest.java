package com.hotelbooking.hotel.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
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

import com.hotelbooking.hotel.dto.request.CreateHotelRequest;
import com.hotelbooking.hotel.dto.response.HotelDetailResponse;
import com.hotelbooking.hotel.dto.response.HotelSearchResponse;
import com.hotelbooking.hotel.enums.City;
import com.hotelbooking.hotel.enums.HotelStatus;
import com.hotelbooking.hotel.enums.Hotel_Category;
import com.hotelbooking.hotel.enums.State;
import com.hotelbooking.hotel.exception.AccessDeniedException;
import com.hotelbooking.hotel.exception.HotelNotFoundException;
import com.hotelbooking.hotel.exception.ValidationException;
import com.hotelbooking.hotel.feign.AuthServiceClient;
import com.hotelbooking.hotel.repository.HotelRepository;
import com.hotelbooking.hotel.service.HotelService;

@ExtendWith(MockitoExtension.class)
class HotelControllerTest {

    @Mock
    private HotelService hotelService;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private AuthServiceClient authServiceClient;

    @InjectMocks
    private HotelController hotelController;

    private CreateHotelRequest createHotelRequest;

    @BeforeEach
    void setUp() {
        createHotelRequest = new CreateHotelRequest();
        createHotelRequest.setName("Test Hotel");
        createHotelRequest.setCategory(Hotel_Category.HOTEL);
        createHotelRequest.setCity(City.DELHI);
        createHotelRequest.setState(State.DELHI);
        createHotelRequest.setCountry("India");
        createHotelRequest.setPincode("110001");
        createHotelRequest.setEmail("test@hotel.com");
        createHotelRequest.setContactNumber("1234567890");
        createHotelRequest.setStatus(HotelStatus.ACTIVE);
        createHotelRequest.setImageUrl("http://example.com/image.jpg");
    }

    @Test
    void testGetMyHotel_Success() {
        HotelDetailResponse hotel = new HotelDetailResponse(1L, "Test Hotel", Hotel_Category.HOTEL, 
                "Description", "Address", City.DELHI, State.DELHI, "India", "110001", 
                "1234567890", "test@hotel.com", 5, "Amenities", HotelStatus.ACTIVE, 10, 5, "http://example.com/image.jpg", null, null);

        when(hotelService.getHotelById(1L)).thenReturn(hotel);

        ResponseEntity<?> response = hotelController.getMyHotel(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetMyHotel_NoHotelId() {
        assertThrows(AccessDeniedException.class, () -> hotelController.getMyHotel(null));
    }

    @Test
    void testSearchHotelsByCity_Success() {
        HotelSearchResponse hotel1 = new HotelSearchResponse(1L, "Hotel 1", Hotel_Category.HOTEL, 
                "Desc", "Addr", City.DELHI, State.DELHI, "India", "110001", "1234567890", 
                "hotel1@test.com", 5, "Amenities", "image.jpg", HotelStatus.ACTIVE, 10, 5);
        
        when(hotelService.searchHotelsByCity(City.DELHI)).thenReturn(Arrays.asList(hotel1));

        ResponseEntity<?> response = hotelController.searchHotelsByCity(City.DELHI, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testSearchHotelsByCategory_Success() {
        HotelSearchResponse hotel1 = new HotelSearchResponse(1L, "Hotel 1", Hotel_Category.RESORT, 
                "Desc", "Addr", City.MUMBAI, State.MAHARASHTRA, "India", "400001", "1234567890", 
                "hotel1@test.com", 5, "Amenities", "image.jpg", HotelStatus.ACTIVE, 10, 5);
        
        when(hotelService.searchHotelsByCategory(Hotel_Category.RESORT)).thenReturn(Arrays.asList(hotel1));

        ResponseEntity<?> response = hotelController.searchHotelsByCity(null, Hotel_Category.RESORT);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testSearchHotels_NoCityOrCategory() {
        assertThrows(ValidationException.class, () -> hotelController.searchHotelsByCity(null, null));
    }

    @Test
    void testGetHotelById_Success() {
        HotelDetailResponse hotel = new HotelDetailResponse(1L, "Test Hotel", Hotel_Category.HOTEL, 
                "Description", "Address", City.DELHI, State.DELHI, "India", "110001", 
                "1234567890", "test@hotel.com", 5, "Amenities", HotelStatus.ACTIVE, 10, 5, "http://example.com/image.jpg", null, null);

        when(hotelService.getHotelById(1L)).thenReturn(hotel);

        ResponseEntity<?> response = hotelController.getHotelById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testCreateHotel_Admin_Success() {
        when(hotelService.createHotel(any(CreateHotelRequest.class))).thenReturn(1L);

        ResponseEntity<?> response = hotelController.createHotel("ADMIN", createHotelRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testCreateHotel_NonAdmin() {
        assertThrows(AccessDeniedException.class, () -> hotelController.createHotel("MANAGER", createHotelRequest));
    }

    @Test
    void testUpdateHotel_Admin_Success() {
        when(hotelService.updateHotel(anyLong(), any(CreateHotelRequest.class))).thenReturn(1L);

        ResponseEntity<?> response = hotelController.updateHotel("ADMIN", null, 1L, createHotelRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testUpdateHotel_Manager_Success() {
        when(hotelService.updateHotel(anyLong(), any(CreateHotelRequest.class))).thenReturn(1L);

        ResponseEntity<?> response = hotelController.updateHotel("MANAGER", 1L, 1L, createHotelRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testUpdateHotel_Manager_WrongHotel() {
        assertThrows(AccessDeniedException.class, () -> hotelController.updateHotel("MANAGER", 1L, 2L, createHotelRequest));
    }

    @Test
    void testUpdateHotel_Manager_NoHotelId() {
        assertThrows(AccessDeniedException.class, () -> hotelController.updateHotel("MANAGER", null, 1L, createHotelRequest));
    }

    @Test
    void testUpdateHotel_NonAdminOrManager() {
        assertThrows(AccessDeniedException.class, () -> hotelController.updateHotel("GUEST", null, 1L, createHotelRequest));
    }

    @Test
    void testGetAllHotels_Admin_Success() {
        HotelDetailResponse hotel = new HotelDetailResponse(1L, "Test Hotel", Hotel_Category.HOTEL, 
                "Description", "Address", City.DELHI, State.DELHI, "India", "110001", 
                "1234567890", "test@hotel.com", 5, "Amenities", HotelStatus.ACTIVE, 10, 5, "http://example.com/image.jpg", null, null);

        when(hotelService.getAllHotels()).thenReturn(Arrays.asList(hotel));

        ResponseEntity<?> response = hotelController.getAllHotels("ADMIN");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetAllHotels_NonAdmin() {
        assertThrows(AccessDeniedException.class, () -> hotelController.getAllHotels("MANAGER"));
    }

    @Test
    void testGetStaffByHotelId_Admin_Success() {
        when(hotelRepository.existsById(1L)).thenReturn(true);
        when(authServiceClient.getUsersByHotelId(1L)).thenReturn(Arrays.asList(Map.of("id", 1L, "name", "Staff")));

        ResponseEntity<?> response = hotelController.getStaffByHotelId("ADMIN", 1L, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetStaffByHotelId_NonAdmin() {
        assertThrows(AccessDeniedException.class, () -> hotelController.getStaffByHotelId("MANAGER", 1L, null));
    }

    @Test
    void testGetStaffByHotelId_HotelNotFound() {
        when(hotelRepository.existsById(999L)).thenReturn(false);

        assertThrows(HotelNotFoundException.class, () -> hotelController.getStaffByHotelId("ADMIN", 999L, null));
    }

    @Test
    void testGetStaffByHotelId_MissingRole() {
        jakarta.servlet.http.HttpServletRequest request = mock(jakarta.servlet.http.HttpServletRequest.class);
        when(request.getHeader("X-User-Role")).thenReturn(null);
        
        assertThrows(ValidationException.class, () -> hotelController.getStaffByHotelId(null, 1L, request));
    }

    @Test
    void testCreateStaff_Admin_Success() {
        when(hotelRepository.existsById(1L)).thenReturn(true);
        when(authServiceClient.createStaff(any())).thenReturn(Map.of("activationToken", "token123"));

        com.hotelbooking.hotel.dto.request.CreateStaffRequestBody requestBody = 
            new com.hotelbooking.hotel.dto.request.CreateStaffRequestBody();
        requestBody.setFullName("John Doe");
        requestBody.setUsername("johndoe");
        requestBody.setEmail("john@test.com");
        requestBody.setPassword("Password123!");
        requestBody.setRole("MANAGER");

        ResponseEntity<?> response = hotelController.createStaff("ADMIN", 1L, requestBody);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testCreateStaff_NonAdmin() {
        assertThrows(AccessDeniedException.class, () -> hotelController.createStaff("MANAGER", 1L, 
            new com.hotelbooking.hotel.dto.request.CreateStaffRequestBody()));
    }

    @Test
    void testCreateStaff_HotelNotFound() {
        when(hotelRepository.existsById(999L)).thenReturn(false);

        assertThrows(HotelNotFoundException.class, () -> hotelController.createStaff("ADMIN", 999L, 
            new com.hotelbooking.hotel.dto.request.CreateStaffRequestBody()));
    }
}

