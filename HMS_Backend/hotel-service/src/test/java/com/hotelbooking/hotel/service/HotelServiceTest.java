package com.hotelbooking.hotel.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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

import com.hotelbooking.hotel.domain.Hotel;
import com.hotelbooking.hotel.dto.request.CreateHotelRequest;
import com.hotelbooking.hotel.dto.response.AvailabilitySearchResponse;
import com.hotelbooking.hotel.dto.response.HotelDetailResponse;
import com.hotelbooking.hotel.dto.response.HotelSearchResponse;
import com.hotelbooking.hotel.enums.City;
import com.hotelbooking.hotel.enums.HotelStatus;
import com.hotelbooking.hotel.enums.Hotel_Category;
import com.hotelbooking.hotel.enums.State;
import com.hotelbooking.hotel.exception.HotelNotFoundException;
import com.hotelbooking.hotel.repository.HotelRepository;
import com.hotelbooking.hotel.repository.RoomRepository;
import com.hotelbooking.hotel.service.RoomAvailabilityService;
import com.hotelbooking.hotel.service.impl.HotelServiceImpl;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomAvailabilityService availabilityService;

    @InjectMocks
    private HotelServiceImpl hotelService;

    private CreateHotelRequest createRequest;
    private Hotel testHotel;

    @BeforeEach
    void setUp() {
        createRequest = new CreateHotelRequest();
        createRequest.setName("Test Hotel");
        createRequest.setCategory(Hotel_Category.HOTEL);
        createRequest.setCity(City.DELHI);
        createRequest.setState(State.DELHI);
        createRequest.setCountry("India");
        createRequest.setPincode("110001");
        createRequest.setEmail("test@hotel.com");
        createRequest.setContactNumber("1234567890");
        createRequest.setStatus(HotelStatus.ACTIVE);
        createRequest.setImageUrl("http://example.com/image.jpg");

        testHotel = Hotel.builder()
                .id(1L)
                .name("Test Hotel")
                .category(Hotel_Category.HOTEL)
                .city(City.DELHI)
                .state(State.DELHI)
                .country("India")
                .pincode("110001")
                .email("test@hotel.com")
                .contactNumber("1234567890")
                .status(HotelStatus.ACTIVE)
                .build();
    }

    @Test
    void testCreateHotel_Success() {
        when(hotelRepository.save(any(Hotel.class))).thenReturn(testHotel);

        Long hotelId = hotelService.createHotel(createRequest);

        assertNotNull(hotelId);
        assertEquals(1L, hotelId);
    }

    @Test
    void testUpdateHotel_Success() {
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(testHotel));
        when(hotelRepository.save(any(Hotel.class))).thenReturn(testHotel);

        Long hotelId = hotelService.updateHotel(1L, createRequest);

        assertEquals(1L, hotelId);
    }

    @Test
    void testUpdateHotel_NotFound() {
        when(hotelRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(HotelNotFoundException.class, () -> hotelService.updateHotel(999L, createRequest));
    }

    @Test
    void testGetHotelById_Success() {
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(testHotel));
        when(roomRepository.countByHotelId(1L)).thenReturn(10L);
        when(availabilityService.searchAvailability(1L, LocalDate.now(), LocalDate.now().plusDays(1)))
                .thenReturn(new AvailabilitySearchResponse(1L, 5, Arrays.asList(1L, 2L, 3L, 4L, 5L)));

        HotelDetailResponse response = hotelService.getHotelById(1L);

        assertNotNull(response);
        assertEquals("Test Hotel", response.getName());
    }

    @Test
    void testGetHotelById_NotFound() {
        when(hotelRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(HotelNotFoundException.class, () -> hotelService.getHotelById(999L));
    }

    @Test
    void testGetHotelById_NoRooms() {
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(testHotel));
        when(roomRepository.countByHotelId(1L)).thenReturn(0L);

        HotelDetailResponse response = hotelService.getHotelById(1L);

        assertNotNull(response);
        assertEquals(0, response.getTotalRooms());
    }

    @Test
    void testSearchHotels_ByCity_Success() {
        Hotel hotel1 = Hotel.builder().id(1L).name("Hotel 1").city(City.DELHI).status(HotelStatus.ACTIVE).build();
        Hotel hotel2 = Hotel.builder().id(2L).name("Hotel 2").city(City.DELHI).status(HotelStatus.ACTIVE).build();

        when(hotelRepository.findByCityAndStatus(City.DELHI, HotelStatus.ACTIVE)).thenReturn(Arrays.asList(hotel1, hotel2));
        when(roomRepository.countByHotelIdAndIsActiveTrue(1L)).thenReturn(10L);
        when(roomRepository.countByHotelIdAndIsActiveTrue(2L)).thenReturn(5L);
        when(availabilityService.searchAvailability(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new AvailabilitySearchResponse(1L, 5, Arrays.asList(1L, 2L, 3L, 4L, 5L)));

        List<HotelSearchResponse> response = hotelService.searchHotels(City.DELHI, 
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), null);

        assertNotNull(response);
        assertEquals(2, response.size());
    }

    @Test
    void testSearchHotels_ByCityAndCategory_Success() {
        Hotel hotel1 = Hotel.builder().id(1L).name("Hotel 1").city(City.DELHI).category(Hotel_Category.RESORT).status(HotelStatus.ACTIVE).build();

        when(hotelRepository.findByCityAndCategoryAndStatus(City.DELHI, Hotel_Category.RESORT, HotelStatus.ACTIVE))
                .thenReturn(Arrays.asList(hotel1));
        when(roomRepository.countByHotelIdAndIsActiveTrue(1L)).thenReturn(10L);
        when(availabilityService.searchAvailability(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new AvailabilitySearchResponse(1L, 5, Arrays.asList(1L, 2L, 3L, 4L, 5L)));

        List<HotelSearchResponse> response = hotelService.searchHotels(City.DELHI, 
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), Hotel_Category.RESORT);

        assertNotNull(response);
        assertEquals(1, response.size());
    }

    @Test
    void testSearchHotels_NoAvailableRooms() {
        Hotel hotel1 = Hotel.builder().id(1L).name("Hotel 1").city(City.DELHI).status(HotelStatus.ACTIVE).build();

        when(hotelRepository.findByCityAndStatus(City.DELHI, HotelStatus.ACTIVE)).thenReturn(Arrays.asList(hotel1));
        when(roomRepository.countByHotelIdAndIsActiveTrue(1L)).thenReturn(10L);
        when(availabilityService.searchAvailability(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new AvailabilitySearchResponse(1L, 0, Arrays.asList()));

        List<HotelSearchResponse> response = hotelService.searchHotels(City.DELHI, 
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), null);

        assertNotNull(response);
        assertEquals(0, response.size());
    }

    @Test
    void testSearchHotels_NoTotalRooms() {
        Hotel hotel1 = Hotel.builder().id(1L).name("Hotel 1").city(City.DELHI).status(HotelStatus.ACTIVE).build();

        when(hotelRepository.findByCityAndStatus(City.DELHI, HotelStatus.ACTIVE)).thenReturn(Arrays.asList(hotel1));
        when(roomRepository.countByHotelIdAndIsActiveTrue(1L)).thenReturn(0L);

        List<HotelSearchResponse> response = hotelService.searchHotels(City.DELHI, 
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), null);

        assertNotNull(response);
        assertEquals(0, response.size());
    }

    @Test
    void testSearchHotels_NoDates() {
        Hotel hotel1 = Hotel.builder().id(1L).name("Hotel 1").city(City.DELHI).status(HotelStatus.ACTIVE).build();

        when(hotelRepository.findByCityAndStatus(City.DELHI, HotelStatus.ACTIVE)).thenReturn(Arrays.asList(hotel1));
        when(roomRepository.countByHotelIdAndIsActiveTrue(1L)).thenReturn(10L);

        List<HotelSearchResponse> response = hotelService.searchHotels(City.DELHI, null, null, null);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(10, response.get(0).getAvailableRooms());
    }

    @Test
    void testGetAllHotels_Success() {
        Hotel hotel1 = Hotel.builder().id(1L).name("Hotel 1").status(HotelStatus.ACTIVE).build();
        Hotel hotel2 = Hotel.builder().id(2L).name("Hotel 2").status(HotelStatus.ACTIVE).build();

        when(hotelRepository.findAll()).thenReturn(Arrays.asList(hotel1, hotel2));
        when(roomRepository.countByHotelId(1L)).thenReturn(10L);
        when(roomRepository.countByHotelId(2L)).thenReturn(5L);
        when(availabilityService.searchAvailability(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new AvailabilitySearchResponse(1L, 5, Arrays.asList(1L, 2L, 3L, 4L, 5L)));

        List<HotelDetailResponse> response = hotelService.getAllHotels();

        assertNotNull(response);
        assertEquals(2, response.size());
    }

    @Test
    void testSearchHotelsByCity_Success() {
        Hotel hotel1 = Hotel.builder().id(1L).name("Hotel 1").city(City.DELHI).status(HotelStatus.ACTIVE).build();

        when(hotelRepository.findByCityAndStatus(City.DELHI, HotelStatus.ACTIVE)).thenReturn(Arrays.asList(hotel1));
        when(roomRepository.countByHotelId(1L)).thenReturn(10L);
        when(availabilityService.searchAvailability(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new AvailabilitySearchResponse(1L, 5, Arrays.asList(1L, 2L, 3L, 4L, 5L)));

        List<HotelSearchResponse> response = hotelService.searchHotelsByCity(City.DELHI);

        assertNotNull(response);
        assertEquals(1, response.size());
    }

    @Test
    void testSearchHotelsByCategory_Success() {
        Hotel hotel1 = Hotel.builder().id(1L).name("Hotel 1").category(Hotel_Category.RESORT).status(HotelStatus.ACTIVE).build();

        when(hotelRepository.findByCategoryAndStatus(Hotel_Category.RESORT, HotelStatus.ACTIVE))
                .thenReturn(Arrays.asList(hotel1));
        when(roomRepository.countByHotelId(1L)).thenReturn(10L);
        when(availabilityService.searchAvailability(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new AvailabilitySearchResponse(1L, 5, Arrays.asList(1L, 2L, 3L, 4L, 5L)));

        List<HotelSearchResponse> response = hotelService.searchHotelsByCategory(Hotel_Category.RESORT);

        assertNotNull(response);
        assertEquals(1, response.size());
    }
}

