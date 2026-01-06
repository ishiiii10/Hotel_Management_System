package com.hotelbooking.hotel.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hotelbooking.hotel.dto.response.HotelDetailResponse;
import com.hotelbooking.hotel.dto.response.RoomResponse;
import com.hotelbooking.hotel.enums.City;
import com.hotelbooking.hotel.enums.HotelStatus;
import com.hotelbooking.hotel.enums.Hotel_Category;
import com.hotelbooking.hotel.enums.RoomCategory;
import com.hotelbooking.hotel.enums.RoomStatus;
import com.hotelbooking.hotel.enums.State;
import com.hotelbooking.hotel.service.HotelService;
import com.hotelbooking.hotel.service.RoomService;

import java.math.BigDecimal;

@ExtendWith(MockitoExtension.class)
class InternalHotelControllerTest {

    @Mock
    private HotelService hotelService;

    @Mock
    private RoomService roomService;

    @InjectMocks
    private InternalHotelController internalHotelController;

    private HotelDetailResponse hotelDetail;
    private RoomResponse roomResponse;

    @BeforeEach
    void setUp() {
        hotelDetail = new HotelDetailResponse(1L, "Test Hotel", Hotel_Category.HOTEL, 
                "Description", "Address", City.DELHI, State.DELHI, "India", "110001", 
                "1234567890", "test@hotel.com", 5, "Amenities", HotelStatus.ACTIVE, 10, 5, "http://example.com/image.jpg", null, null);

        roomResponse = new RoomResponse(1L, 1L, "101", RoomCategory.STANDARD, 
                BigDecimal.valueOf(1000), 2, 1, "Double", 200, "AC, TV", "Nice room", 
                RoomStatus.AVAILABLE, true);
    }

    @Test
    void testGetHotelById_Success() {
        when(hotelService.getHotelById(1L)).thenReturn(hotelDetail);

        HotelDetailResponse response = internalHotelController.getHotelById(1L);

        assertNotNull(response);
        assertEquals("Test Hotel", response.getName());
    }

    @Test
    void testGetRoomsByHotel_Success() {
        when(roomService.getRoomsByHotel(1L)).thenReturn(Arrays.asList(roomResponse));

        List<RoomResponse> response = internalHotelController.getRoomsByHotel(1L);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("101", response.get(0).getRoomNumber());
    }

    @Test
    void testGetRoomById_Success() {
        when(roomService.getRoomById(1L)).thenReturn(roomResponse);

        RoomResponse response = internalHotelController.getRoomById(1L);

        assertNotNull(response);
        assertEquals("101", response.getRoomNumber());
    }
}

