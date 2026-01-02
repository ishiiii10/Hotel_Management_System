package com.hotelbooking.hotel.service.impl;



import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelbooking.hotel.domain.Room;
import com.hotelbooking.hotel.dto.request.CreateRoomRequest;
import com.hotelbooking.hotel.dto.response.RoomResponse;
import com.hotelbooking.hotel.enums.RoomStatus;
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


    @Override
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
    public List<RoomResponse> getRoomsByHotel(Long hotelId) {

        return roomRepository.findByHotelId(hotelId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public Long updateRoom(Long roomId, CreateRoomRequest request) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalStateException("Room not found"));

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
        return room.getId();
    }

    @Override
    public void updateRoomStatus(Long roomId, RoomStatus status) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalStateException("Room not found"));

        room.setStatus(status);
        roomRepository.save(room);
    }

    @Override
    public void updateRoomActiveStatus(Long roomId, boolean isActive) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalStateException("Room not found"));

        room.setIsActive(isActive);
        roomRepository.save(room);
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
    public void deleteRoom(Long roomId) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalStateException("Room not found"));

        if (!room.getIsActive()) {
            throw new IllegalStateException("Room already deleted");
        }

        room.setIsActive(false);
        room.setStatus(RoomStatus.INACTIVE);

        roomRepository.save(room);
    }
}
    
