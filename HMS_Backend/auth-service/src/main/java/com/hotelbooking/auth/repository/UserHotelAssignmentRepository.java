package com.hotelbooking.auth.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotelbooking.auth.domain.UserHotelAssignment;


public interface UserHotelAssignmentRepository extends JpaRepository<UserHotelAssignment, Long> {

    List<UserHotelAssignment> findByUserId(Long userId);
}