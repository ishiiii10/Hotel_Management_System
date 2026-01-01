package com.hotelbooking.hotel.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelbooking.hotel.domain.RoomInventory;
import com.hotelbooking.hotel.repository.RoomInventoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomInventoryService {

    private final RoomInventoryRepository repository;

    @Transactional
    public RoomInventory upsertInventory(
            Long hotelId,
            Long categoryId,
            int totalRooms
    ) {
        RoomInventory inventory = repository
                .findByHotelIdAndCategoryId(hotelId, categoryId)
                .orElse(RoomInventory.builder()
                        .hotelId(hotelId)
                        .categoryId(categoryId)
                        .outOfService(0)
                        .build());

        if (totalRooms < inventory.getOutOfService()) {
            throw new com.hotelbooking.hotel.exception.HotelException(
                com.hotelbooking.hotel.exception.HotelErrorCode.INVENTORY_RULE_VIOLATION,
                "Total rooms cannot be less than rooms under maintenance",
                org.springframework.http.HttpStatus.BAD_REQUEST
            );
        }

        inventory.setTotalRooms(totalRooms);
        return repository.save(inventory);
    }

    public List<RoomInventory> getInventory(Long hotelId) {
        return repository.findByHotelId(hotelId);
    }

    /* -------- Internal hooks (used later by physical rooms) -------- */

    @Transactional
    public void incrementOutOfService(Long hotelId, Long categoryId) {
        RoomInventory inventory = repository
                .findByHotelIdAndCategoryId(hotelId, categoryId)
                .orElseThrow(() ->
                        new IllegalStateException("Inventory not configured"));

        if (inventory.getOutOfService() >= inventory.getTotalRooms()) {
            throw new com.hotelbooking.hotel.exception.HotelException(
                com.hotelbooking.hotel.exception.HotelErrorCode.INVENTORY_RULE_VIOLATION,
                "No rooms available to mark maintenance",
                org.springframework.http.HttpStatus.BAD_REQUEST
            );
        }

        inventory.setOutOfService(inventory.getOutOfService() + 1);
        repository.save(inventory);
    }

    @Transactional
    public void decrementOutOfService(Long hotelId, Long categoryId) {
        RoomInventory inventory = repository
                .findByHotelIdAndCategoryId(hotelId, categoryId)
                .orElseThrow(() ->
                        new IllegalStateException("Inventory not configured"));

        if (inventory.getOutOfService() <= 0) {
            throw new com.hotelbooking.hotel.exception.HotelException(
                com.hotelbooking.hotel.exception.HotelErrorCode.INVENTORY_RULE_VIOLATION,
                "Out-of-service count already zero",
                org.springframework.http.HttpStatus.BAD_REQUEST
            );
        }

        inventory.setOutOfService(inventory.getOutOfService() - 1);
        repository.save(inventory);
    }
}