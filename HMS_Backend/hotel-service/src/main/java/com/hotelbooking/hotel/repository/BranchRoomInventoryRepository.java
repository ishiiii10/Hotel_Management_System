package com.hotelbooking.hotel.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotelbooking.hotel.domain.BranchRoomInventory;

public interface BranchRoomInventoryRepository extends JpaRepository<BranchRoomInventory, Long> {

	Optional<BranchRoomInventory> findByBranchIdAndRoomCategoryId(Long branchId, Long categoryId);

	List<BranchRoomInventory> findByBranchId(Long branchId);
}
