package com.hotelbooking.hotel.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import com.hotelbooking.hotel.domain.Room;
import com.hotelbooking.hotel.domain.RoomAvailability;
import com.hotelbooking.hotel.dto.request.CreateRoomRequest;
import com.hotelbooking.hotel.dto.response.RoomResponse;
import com.hotelbooking.hotel.enums.AvailabilityStatus;
import com.hotelbooking.hotel.enums.RoomCategory;
import com.hotelbooking.hotel.enums.RoomStatus;
import com.hotelbooking.hotel.exception.RoomAlreadyExistsException;
import com.hotelbooking.hotel.exception.RoomNotFoundException;
import com.hotelbooking.hotel.repository.RoomAvailabilityRepository;
import com.hotelbooking.hotel.repository.RoomRepository;
import com.hotelbooking.hotel.service.impl.RoomServiceImpl;
import com.hotelbooking.hotel.util.RoomAvailabilityGenerator;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomAvailabilityGenerator availabilityGenerator;

    @Mock
    private RoomAvailabilityRepository availabilityRepository;

    @Mock
    private CacheManager cacheManager;

    @InjectMocks
    private RoomServiceImpl roomService;

    private CreateRoomRequest createRequest;
    private Room testRoom;

    @BeforeEach
    void setUp() {
        createRequest = new CreateRoomRequest();
        createRequest.setHotelId(1L);
        createRequest.setRoomNumber("101");
        createRequest.setRoomType(RoomCategory.STANDARD);
        createRequest.setPricePerNight(BigDecimal.valueOf(1000));
        createRequest.setMaxOccupancy(2);
        createRequest.setStatus(RoomStatus.AVAILABLE);
        createRequest.setIsActive(true);

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

        // Lenient stubbing for cacheManager since it may or may not be called depending on cache state
        org.mockito.Mockito.lenient().when(cacheManager.getCache("roomsByHotel"))
                .thenReturn(new ConcurrentMapCacheManager().getCache("roomsByHotel"));
    }

    @Test
    void testCreateRoom_Success() {
        when(roomRepository.existsByHotelIdAndRoomNumber(1L, "101")).thenReturn(false);
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);
        doNothing().when(availabilityGenerator).generateForRoom(anyLong(), anyLong());

        Long roomId = roomService.createRoom(createRequest);

        assertNotNull(roomId);
        assertEquals(1L, roomId);
        verify(availabilityGenerator).generateForRoom(1L, 1L);
    }

    @Test
    void testCreateRoom_RoomAlreadyExists() {
        when(roomRepository.existsByHotelIdAndRoomNumber(1L, "101")).thenReturn(true);

        assertThrows(RoomAlreadyExistsException.class, () -> roomService.createRoom(createRequest));
    }

    @Test
    void testGetRoomById_Success() {
        when(roomRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(testRoom));

        RoomResponse response = roomService.getRoomById(1L);

        assertNotNull(response);
        assertEquals("101", response.getRoomNumber());
    }

    @Test
    void testGetRoomById_NotFound() {
        when(roomRepository.findByIdAndIsActiveTrue(999L)).thenReturn(Optional.empty());

        assertThrows(RoomNotFoundException.class, () -> roomService.getRoomById(999L));
    }

    @Test
    void testGetRoomsByHotel_Success() {
        Room room1 = Room.builder().id(1L).hotelId(1L).roomNumber("101").roomCategory(RoomCategory.STANDARD)
                .pricePerNight(BigDecimal.valueOf(1000)).maxOccupancy(2).status(RoomStatus.AVAILABLE).isActive(true).build();
        Room room2 = Room.builder().id(2L).hotelId(1L).roomNumber("102").roomCategory(RoomCategory.DELUXE)
                .pricePerNight(BigDecimal.valueOf(2000)).maxOccupancy(3).status(RoomStatus.AVAILABLE).isActive(true).build();

        when(roomRepository.findByHotelId(1L)).thenReturn(Arrays.asList(room1, room2));

        List<RoomResponse> response = roomService.getRoomsByHotel(1L);

        assertNotNull(response);
        assertEquals(2, response.size());
    }

    @Test
    void testUpdateRoom_Success() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        Long roomId = roomService.updateRoom(1L, createRequest);

        assertEquals(1L, roomId);
    }

    @Test
    void testUpdateRoom_NotFound() {
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RoomNotFoundException.class, () -> roomService.updateRoom(999L, createRequest));
    }

    @Test
    void testUpdateRoomStatus_Success() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);
        when(availabilityRepository.findByRoomIdAndDateBetween(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList());

        roomService.updateRoomStatus(1L, RoomStatus.MAINTENANCE);

        verify(roomRepository).save(any(Room.class));
    }

    @Test
    void testUpdateRoomStatus_NotFound() {
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RoomNotFoundException.class, () -> roomService.updateRoomStatus(999L, RoomStatus.MAINTENANCE));
    }

    @Test
    void testUpdateRoomStatus_UpdatesAvailability() {
        RoomAvailability availability = RoomAvailability.builder()
                .roomId(1L)
                .date(LocalDate.now().plusDays(1))
                .status(AvailabilityStatus.AVAILABLE)
                .build();

        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);
        when(availabilityRepository.findByRoomIdAndDateBetween(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(availability));
        when(availabilityRepository.save(any(RoomAvailability.class))).thenReturn(availability);

        roomService.updateRoomStatus(1L, RoomStatus.INACTIVE);

        verify(availabilityRepository).save(any(RoomAvailability.class));
    }

    @Test
    void testUpdateRoomActiveStatus_Success() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);
        when(availabilityRepository.findByRoomIdAndDateBetween(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList());

        roomService.updateRoomActiveStatus(1L, false);

        verify(roomRepository).save(any(Room.class));
    }

    @Test
    void testUpdateRoomActiveStatus_NotFound() {
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RoomNotFoundException.class, () -> roomService.updateRoomActiveStatus(999L, false));
    }

    @Test
    void testDeleteRoom_Success() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        roomService.deleteRoom(1L);

        verify(roomRepository).save(any(Room.class));
    }

    @Test
    void testDeleteRoom_NotFound() {
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RoomNotFoundException.class, () -> roomService.deleteRoom(999L));
    }

    @Test
    void testDeleteRoom_AlreadyDeleted() {
        Room deletedRoom = Room.builder()
                .id(1L)
                .isActive(false)
                .build();

        when(roomRepository.findById(1L)).thenReturn(Optional.of(deletedRoom));

        assertThrows(RoomNotFoundException.class, () -> roomService.deleteRoom(1L));
    }

    @Test
    void testUpdateRoomStatus_ReservedNotChanged() {
        RoomAvailability reserved = RoomAvailability.builder()
                .roomId(1L)
                .date(LocalDate.now().plusDays(1))
                .status(AvailabilityStatus.RESERVED)
                .build();

        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);
        when(availabilityRepository.findByRoomIdAndDateBetween(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(reserved));

        roomService.updateRoomStatus(1L, RoomStatus.INACTIVE);

        verify(availabilityRepository, never()).save(any(RoomAvailability.class));
    }
}

