package com.hotelbooking.hotel.repository;

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

import com.hotelbooking.hotel.domain.Hotel;
import com.hotelbooking.hotel.enums.City;
import com.hotelbooking.hotel.enums.HotelStatus;
import com.hotelbooking.hotel.enums.Hotel_Category;
import com.hotelbooking.hotel.enums.State;

@ExtendWith(MockitoExtension.class)
class HotelRepositoryTest {

    @Mock
    private HotelRepository hotelRepository;

    private Hotel testHotel;

    @BeforeEach
    void setUp() {
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
    void testFindByCity_Success() {
        Hotel hotel2 = Hotel.builder().id(2L).name("Hotel 2").city(City.DELHI).build();

        when(hotelRepository.findByCity(City.DELHI)).thenReturn(Arrays.asList(testHotel, hotel2));

        List<Hotel> hotels = hotelRepository.findByCity(City.DELHI);

        assertNotNull(hotels);
        assertEquals(2, hotels.size());
    }

    @Test
    void testFindByCityAndStatus_Success() {
        when(hotelRepository.findByCityAndStatus(City.DELHI, HotelStatus.ACTIVE))
                .thenReturn(Arrays.asList(testHotel));

        List<Hotel> hotels = hotelRepository.findByCityAndStatus(City.DELHI, HotelStatus.ACTIVE);

        assertNotNull(hotels);
        assertEquals(1, hotels.size());
    }

    @Test
    void testFindByCityAndCategoryAndStatus_Success() {
        when(hotelRepository.findByCityAndCategoryAndStatus(City.DELHI, Hotel_Category.RESORT, HotelStatus.ACTIVE))
                .thenReturn(Arrays.asList(testHotel));

        List<Hotel> hotels = hotelRepository.findByCityAndCategoryAndStatus(City.DELHI, Hotel_Category.RESORT, HotelStatus.ACTIVE);

        assertNotNull(hotels);
        assertEquals(1, hotels.size());
    }

    @Test
    void testFindByCategoryAndStatus_Success() {
        when(hotelRepository.findByCategoryAndStatus(Hotel_Category.RESORT, HotelStatus.ACTIVE))
                .thenReturn(Arrays.asList(testHotel));

        List<Hotel> hotels = hotelRepository.findByCategoryAndStatus(Hotel_Category.RESORT, HotelStatus.ACTIVE);

        assertNotNull(hotels);
        assertEquals(1, hotels.size());
    }

    @Test
    void testFindById_Success() {
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(testHotel));

        Optional<Hotel> hotel = hotelRepository.findById(1L);

        assertTrue(hotel.isPresent());
        assertEquals("Test Hotel", hotel.get().getName());
    }

    @Test
    void testFindById_NotFound() {
        when(hotelRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Hotel> hotel = hotelRepository.findById(999L);

        assertFalse(hotel.isPresent());
    }

    @Test
    void testExistsById_True() {
        when(hotelRepository.existsById(1L)).thenReturn(true);

        boolean exists = hotelRepository.existsById(1L);

        assertTrue(exists);
    }

    @Test
    void testExistsById_False() {
        when(hotelRepository.existsById(999L)).thenReturn(false);

        boolean exists = hotelRepository.existsById(999L);

        assertFalse(exists);
    }
}

