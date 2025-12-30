package com.hotelbooking.hotel.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.hotelbooking.hotel.domain.RoomHold;
import com.hotelbooking.hotel.domain.RoomInventory;
import com.hotelbooking.hotel.dto.CreateHoldRequest;
import com.hotelbooking.hotel.repository.RoomHoldRepository;
import com.hotelbooking.hotel.repository.RoomInventoryRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomHoldService {

    private final RoomInventoryRepository inventoryRepository;
    private final RoomHoldRepository holdRepository;

    private static final int HOLD_TTL_MINUTES = 15;

    @Transactional
    public RoomHold createHold(CreateHoldRequest request) {

        RoomInventory inventory = inventoryRepository
                .findByHotelIdAndCategoryId(
                        request.getHotelId(),
                        request.getCategoryId()
                )
                .orElseThrow(() ->
                        new IllegalStateException("Inventory not configured"));

        int activeHolds = holdRepository.countActiveHolds(
                request.getHotelId(),
                request.getCategoryId(),
                request.getCheckInDate(),
                request.getCheckOutDate(),
                LocalDateTime.now()
        );

        int available =
                inventory.getAvailableRooms() - activeHolds;

        if (available < request.getRooms()) {
            throw new IllegalStateException("Insufficient availability");
        }

        RoomHold hold = RoomHold.builder()
                .holdId("HOLD-" + UUID.randomUUID().toString().substring(0, 6))
                .hotelId(request.getHotelId())
                .categoryId(request.getCategoryId())
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .rooms(request.getRooms())
                .expiresAt(LocalDateTime.now().plusMinutes(HOLD_TTL_MINUTES))
                .released(false)
                .build();

        return holdRepository.save(hold);
    }
    
    @Transactional
    public void releaseHold(String holdId) {

        RoomHold hold = holdRepository.findByHoldId(holdId)
                .orElseThrow(() -> new IllegalStateException("Hold not found"));

        if (hold.isReleased()) {
            return; // idempotent
        }

        hold.setReleased(true);
        holdRepository.save(hold);
    }
    
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void expireHolds() {
        List<RoomHold> expired =
                holdRepository.findByExpiresAtBeforeAndReleasedFalse(LocalDateTime.now());

        expired.forEach(h -> h.setReleased(true));
    }
}
