package com.hotelbooking.hotel.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hotelbooking.hotel.domain.PhysicalRoom;
import com.hotelbooking.hotel.domain.RoomState;
import com.hotelbooking.hotel.repository.PhysicalRoomRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PhysicalRoomService {

    private final PhysicalRoomRepository repository;

    public List<PhysicalRoom> registerRooms(
            Long hotelId,
            Long categoryId,
            List<PhysicalRoom> rooms
    ) {
        rooms.forEach(r -> {
            r.setHotelId(hotelId);
            r.setCategoryId(categoryId);
            r.setState(RoomState.AVAILABLE);
        });
        return repository.saveAll(rooms);
    }

    public PhysicalRoom getRoom(Long roomId) {
        return repository.findById(roomId)
                .orElseThrow(() -> new IllegalStateException("Room not found"));
    }

    public PhysicalRoom assignRoom(Long roomId) {
        PhysicalRoom room = getRoom(roomId);

        if (room.getState() != RoomState.AVAILABLE) {
            throw new IllegalStateException("Room is not available");
        }

        room.setState(RoomState.OCCUPIED);
        return repository.save(room);
    }

    public PhysicalRoom releaseRoom(Long roomId) {
        PhysicalRoom room = getRoom(roomId);

        if (room.getState() != RoomState.OCCUPIED) {
            throw new IllegalStateException("Room is not occupied");
        }

        room.setState(RoomState.AVAILABLE);
        return repository.save(room);
    }

    public PhysicalRoom updateMaintenance(Long roomId, boolean underMaintenance) {
        PhysicalRoom room = getRoom(roomId);

        if (underMaintenance && room.getState() != RoomState.AVAILABLE) {
            throw new IllegalStateException("Room must be available to enter maintenance");
        }

        if (!underMaintenance && room.getState() != RoomState.UNDER_MAINTENANCE) {
            throw new IllegalStateException("Room is not under maintenance");
        }

        room.setState(
                underMaintenance
                        ? RoomState.UNDER_MAINTENANCE
                        : RoomState.AVAILABLE
        );

        return repository.save(room);
    }
}