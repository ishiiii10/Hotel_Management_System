package com.hotelbooking.hotel.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.hotelbooking.hotel.domain.RoomInventory;
import com.hotelbooking.hotel.repository.RoomHoldRepository;
import com.hotelbooking.hotel.repository.RoomInventoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final RoomInventoryRepository inventoryRepository;
    private final RoomHoldRepository holdRepository;

    public int getAvailability(
            Long hotelId,
            Long categoryId,
            LocalDate checkIn,
            LocalDate checkOut
    ) {
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Invalid date range");
        }

        RoomInventory inventory = inventoryRepository
                .findByHotelIdAndCategoryId(hotelId, categoryId)
                .orElseThrow(() ->
                        new IllegalStateException("Inventory not configured"));

        int activeHolds = holdRepository.countActiveHolds(
                hotelId,
                categoryId,
                checkIn,
                checkOut,
                LocalDateTime.now()
        );

        int available = inventory.getAvailableRooms() - activeHolds;
        return Math.max(available, 0);
    }
}