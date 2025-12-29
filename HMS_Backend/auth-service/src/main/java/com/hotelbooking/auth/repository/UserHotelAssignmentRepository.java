package com.hotelbooking.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotelbooking.auth.domain.UserHotelAssignment;

public interface UserHotelAssignmentRepository
        extends JpaRepository<UserHotelAssignment, Long> {

    Optional<UserHotelAssignment> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}