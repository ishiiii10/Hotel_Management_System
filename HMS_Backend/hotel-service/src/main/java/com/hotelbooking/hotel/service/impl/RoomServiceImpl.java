package com.hotelbooking.hotel.service.impl;



import java.time.LocalDate;
import java.util.List;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelbooking.hotel.domain.Room;
import com.hotelbooking.hotel.domain.RoomAvailability;
import com.hotelbooking.hotel.dto.request.CreateRoomRequest;
import com.hotelbooking.hotel.dto.response.RoomResponse;
import com.hotelbooking.hotel.enums.AvailabilityStatus;
import com.hotelbooking.hotel.enums.RoomStatus;
import com.hotelbooking.hotel.repository.RoomAvailabilityRepository;
import com.hotelbooking.hotel.repository.RoomRepository;
import com.hotelbooking.hotel.service.RoomService;
import com.hotelbooking.hotel.util.RoomAvailabilityGenerator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomAvailabilityGenerator availabilityGenerator;
    private final RoomAvailabilityRepository availabilityRepository;
    private final CacheManager cacheManager;


    @Override
    @CacheEvict(value = "roomsByHotel", key = "#request.hotelId")
    public Long createRoom(CreateRoomRequest request) {

        if (roomRepository.existsByHotelIdAndRoomNumber(
                request.getHotelId(),
                request.getRoomNumber())) {

            throw new IllegalStateException(
                    "Room number " + request.getRoomNumber() + " already exists for this hotel"
            );
        }

        Room room = Room.builder()
                .hotelId(request.getHotelId())
                .roomNumber(request.getRoomNumber())
                .roomCategory(request.getRoomType())
                .pricePerNight(request.getPricePerNight())
                .maxOccupancy(request.getMaxOccupancy())
                .floorNumber(request.getFloorNumber())
                .bedType(request.getBedType())
                .roomSize(request.getRoomSize())
                .amenities(request.getAmenities())
                .description(request.getDescription())
                .status(request.getStatus())
                .isActive(request.getIsActive())
                .build();

        Room savedRoom = roomRepository.save(room);

        // AUTO-GENERATE AVAILABILITY
        availabilityGenerator.generateForRoom(
                savedRoom.getHotelId(),
                savedRoom.getId()
        );

        return savedRoom.getId();
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "rooms", key = "#roomId", unless = "#result == null")
    public RoomResponse getRoomById(Long roomId) {

        Room room = roomRepository.findByIdAndIsActiveTrue(roomId)
                .orElseThrow(() -> new IllegalStateException("Room not found"));

        return new RoomResponse(
                room.getId(),
                room.getHotelId(),
                room.getRoomNumber(),
                room.getRoomCategory(),
                room.getPricePerNight(),
                room.getMaxOccupancy(),
                room.getFloorNumber(),
                room.getBedType(),
                room.getRoomSize(),
                room.getAmenities(),
                room.getDescription(),
                room.getStatus(),
                room.getIsActive()
        );
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "roomsByHotel", key = "#hotelId")
    public List<RoomResponse> getRoomsByHotel(Long hotelId) {

        return roomRepository.findByHotelId(hotelId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @CacheEvict(value = "rooms", key = "#roomId")
    public Long updateRoom(Long roomId, CreateRoomRequest request) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalStateException("Room not found"));

        Long oldHotelId = room.getHotelId();

        room.setRoomCategory(request.getRoomType());
        room.setPricePerNight(request.getPricePerNight());
        room.setMaxOccupancy(request.getMaxOccupancy());
        room.setFloorNumber(request.getFloorNumber());
        room.setBedType(request.getBedType());
        room.setRoomSize(request.getRoomSize());
        room.setAmenities(request.getAmenities());
        room.setDescription(request.getDescription());
        room.setStatus(request.getStatus());
        room.setIsActive(request.getIsActive());

        roomRepository.save(room);
        
        // Evict roomsByHotel cache for old hotel and new hotel (if hotel changed)
        evictRoomsByHotelCache(oldHotelId);
        if (!oldHotelId.equals(request.getHotelId())) {
            evictRoomsByHotelCache(request.getHotelId());
        }
        return room.getId();
    }

    @Override
    @CacheEvict(value = "rooms", key = "#roomId")
    public void updateRoomStatus(Long roomId, RoomStatus status) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalStateException("Room not found"));
        Long hotelId = room.getHotelId();
        room.setStatus(status);
        roomRepository.save(room);

        // Update RoomAvailability for future dates
        updateAvailabilityForRoomStatus(room, status, room.getIsActive());
        
        // Evict roomsByHotel cache for this hotel
        evictRoomsByHotelCache(hotelId);
    }

    @Override
    @CacheEvict(value = "rooms", key = "#roomId")
    public void updateRoomActiveStatus(Long roomId, boolean isActive) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalStateException("Room not found"));
        Long hotelId = room.getHotelId();
        room.setIsActive(isActive);
        roomRepository.save(room);

        // Update RoomAvailability for future dates
        updateAvailabilityForRoomStatus(room, room.getStatus(), isActive);
        
        // Evict roomsByHotel cache for this hotel
        evictRoomsByHotelCache(hotelId);
    }

    private void updateAvailabilityForRoomStatus(Room room, RoomStatus status, boolean isActive) {
        // Only update for future dates
        LocalDate today = LocalDate.now();
        List<RoomAvailability> futureAvailabilities = availabilityRepository.findByRoomIdAndDateBetween(
                room.getId(), today, today.plusYears(2)); // Adjust range as needed
        AvailabilityStatus newStatus;
        if (!isActive || status == RoomStatus.INACTIVE || status == RoomStatus.MAINTENANCE || status == RoomStatus.OUT_OF_SERVICE) {
            newStatus = AvailabilityStatus.UNAVAILABLE;
        } else if (status == RoomStatus.AVAILABLE && isActive) {
            newStatus = AvailabilityStatus.AVAILABLE;
        } else {
            return; // No change
        }
        for (RoomAvailability availability : futureAvailabilities) {
            if (availability.getStatus() != AvailabilityStatus.RESERVED && availability.getStatus() != AvailabilityStatus.BLOCKED) {
                availability.setStatus(newStatus);
                availability.setSource("SYSTEM: status sync");
                availabilityRepository.save(availability);
            }
        }
    }

    private RoomResponse toDto(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getHotelId(),
                room.getRoomNumber(),
                room.getRoomCategory(),
                room.getPricePerNight(),
                room.getMaxOccupancy(),
                room.getFloorNumber(),
                room.getBedType(),
                room.getRoomSize(),
                room.getAmenities(),
                room.getDescription(),
                room.getStatus(),
                room.getIsActive()
        );
    }

    @Override
    @Transactional
    @CacheEvict(value = "rooms", key = "#roomId")
    public void deleteRoom(Long roomId) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalStateException("Room not found"));

        if (!room.getIsActive()) {
            throw new IllegalStateException("Room already deleted");
        }
        
        Long hotelId = room.getHotelId();

        room.setIsActive(false);
        room.setStatus(RoomStatus.INACTIVE);

        roomRepository.save(room);
        
        // Evict roomsByHotel cache for this hotel
        evictRoomsByHotelCache(hotelId);
    }
    
    private void evictRoomsByHotelCache(Long hotelId) {
        var cache = cacheManager.getCache("roomsByHotel");
        if (cache != null) {
            cache.evict(hotelId);
        }
    }
}
    
