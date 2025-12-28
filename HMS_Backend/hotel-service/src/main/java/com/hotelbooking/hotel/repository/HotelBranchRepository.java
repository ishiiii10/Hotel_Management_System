package com.hotelbooking.hotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotelbooking.hotel.domain.HotelBranch;

public interface HotelBranchRepository extends JpaRepository<HotelBranch, Long> {
}
