package com.hotelbooking.billing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotelbooking.billing.domain.Bill;

public interface BillRepository extends JpaRepository<Bill, Long> {

    Optional<Bill> findByBookingId(Long bookingId);

    List<Bill> findByUserId(Long userId);

    List<Bill> findAll();
}

