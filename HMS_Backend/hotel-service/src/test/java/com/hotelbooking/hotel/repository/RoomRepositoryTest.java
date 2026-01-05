package com.hotelbooking.hotel.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hotelbooking.hotel.domain.Room;
import com.hotelbooking.hotel.enums.RoomCategory;
import com.hotelbooking.hotel.enums.RoomStatus;

@ExtendWith(MockitoExtension.class)
class RoomRepositoryTest {

    @Mock
    private RoomRepository roomRepository;

    private Room testRoom;

    @BeforeEach
    void setUp() {
        testRoom = Room.builder()
                .id(1L)
                .hotelId(1L)
                .roomNumber("101")
                .roomCategory(RoomCategory.STANDARD)
                .pricePerNight(BigDecimal.valueOf(1000))
                .maxOccupancy(2)
                .status(RoomStatus.AVAILABLE)
                .isActive(true)
                .build();
    }

    @Test
    void testFindByHotelId_Success() {
        Room room2 = Room.builder().id(2L).hotelId(1L).roomNumber("102").roomCategory(RoomCategory.DELUXE)
                .pricePerNight(BigDecimal.valueOf(2000)).maxOccupancy(3).status(RoomStatus.AVAILABLE).isActive(true).build();

        when(roomRepository.findByHotelId(1L)).thenReturn(Arrays.asList(testRoom, room2));

        List<Room> rooms = roomRepository.findByHotelId(1L);

        assertNotNull(rooms);
        assertEquals(2, rooms.size());
    }

    @Test
    void testFindByIdAndIsActiveTrue_Success() {
        when(roomRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(testRoom));

        Optional<Room> room = roomRepository.findByIdAndIsActiveTrue(1L);

        assertTrue(room.isPresent());
        assertEquals("101", room.get().getRoomNumber());
    }

    @Test
    void testFindByIdAndIsActiveTrue_NotFound() {
        when(roomRepository.findByIdAndIsActiveTrue(999L)).thenReturn(Optional.empty());

        Optional<Room> room = roomRepository.findByIdAndIsActiveTrue(999L);

        assertFalse(room.isPresent());
    }

    @Test
    void testExistsByHotelIdAndRoomNumber_True() {
        when(roomRepository.existsByHotelIdAndRoomNumber(1L, "101")).thenReturn(true);

        boolean exists = roomRepository.existsByHotelIdAndRoomNumber(1L, "101");

        assertTrue(exists);
    }

    @Test
    void testExistsByHotelIdAndRoomNumber_False() {
        when(roomRepository.existsByHotelIdAndRoomNumber(1L, "999")).thenReturn(false);

        boolean exists = roomRepository.existsByHotelIdAndRoomNumber(1L, "999");

        assertFalse(exists);
    }

    @Test
    void testCountByHotelId_Success() {
        when(roomRepository.countByHotelId(1L)).thenReturn(10L);

        long count = roomRepository.countByHotelId(1L);

        assertEquals(10L, count);
    }

    @Test
    void testCountByHotelIdAndIsActiveTrue_Success() {
        when(roomRepository.countByHotelIdAndIsActiveTrue(1L)).thenReturn(8L);

        long count = roomRepository.countByHotelIdAndIsActiveTrue(1L);

        assertEquals(8L, count);
    }

    @Test
    void testFindById_Success() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));

        Optional<Room> room = roomRepository.findById(1L);

        assertTrue(room.isPresent());
        assertEquals("101", room.get().getRoomNumber());
    }

    @Test
    void testFindByIdAndHotelId_Success() {
        when(roomRepository.findByIdAndHotelId(1L, 1L)).thenReturn(Optional.of(testRoom));

        Optional<Room> room = roomRepository.findByIdAndHotelId(1L, 1L);

        assertTrue(room.isPresent());
        assertEquals(1L, room.get().getHotelId());
    }
}

