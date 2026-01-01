package com.hotelbooking.hotel.service;

import java.time.LocalDate;

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
            throw new com.hotelbooking.hotel.exception.HotelException(
                com.hotelbooking.hotel.exception.HotelErrorCode.VALIDATION_ERROR,
                "Invalid date range",
                org.springframework.http.HttpStatus.BAD_REQUEST
            );
        }

        RoomInventory inventory = inventoryRepository
                .findByHotelIdAndCategoryId(hotelId, categoryId)
                .orElseThrow(() ->
                        new com.hotelbooking.hotel.exception.HotelException(
                            com.hotelbooking.hotel.exception.HotelErrorCode.NOT_FOUND,
                            "Inventory not configured",
                            org.springframework.http.HttpStatus.NOT_FOUND
                        )
                );

        int activeHolds = holdRepository.countActiveHolds(
                hotelId,
                categoryId,
                checkIn,
                checkOut,
                java.time.LocalDateTime.now()
        );

        int available = inventory.getTotalRooms() - inventory.getOutOfService() - activeHolds;
        if (available < 0) {
            throw new com.hotelbooking.hotel.exception.HotelException(
                com.hotelbooking.hotel.exception.HotelErrorCode.INVENTORY_RULE_VIOLATION,
                "Inventory rule violated: booked+maintenance+holds exceeds total",
                org.springframework.http.HttpStatus.BAD_REQUEST
            );
        }
        return available;
    }
}