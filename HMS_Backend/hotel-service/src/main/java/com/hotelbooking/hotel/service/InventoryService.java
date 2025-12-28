package com.hotelbooking.hotel.service;

import org.springframework.stereotype.Service;

import com.hotelbooking.hotel.domain.BranchRoomInventory;
import com.hotelbooking.hotel.domain.HotelBranch;
import com.hotelbooking.hotel.domain.RoomCategory;
import com.hotelbooking.hotel.dto.InventoryRequest;
import com.hotelbooking.hotel.repository.BranchRoomInventoryRepository;
import com.hotelbooking.hotel.repository.HotelBranchRepository;
import com.hotelbooking.hotel.repository.RoomCategoryRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final HotelBranchRepository branchRepository;
    private final RoomCategoryRepository categoryRepository;
    private final BranchRoomInventoryRepository inventoryRepository;

    @Transactional
    public BranchRoomInventory upsertInventory(
            Long branchId,
            Long managerId,
            InventoryRequest request
    ) {
        HotelBranch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found"));

        RoomCategory category = categoryRepository.findById(request.getRoomCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Room category not found"));

        BranchRoomInventory inventory = inventoryRepository
                .findByBranchIdAndRoomCategoryId(branchId, category.getId())
                .orElse(
                    BranchRoomInventory.builder()
                        .branch(branch)
                        .roomCategory(category)
                        .build()
                );

        inventory.setTotalRooms(request.getTotalRooms());
        inventory.setPriceOverride(request.getPriceOverride());
        inventory.setLastUpdatedBy(managerId);

        return inventoryRepository.save(inventory);
    }
}