package com.hotelbooking.hotel.controller;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotelbooking.hotel.domain.HotelBranch;
import com.hotelbooking.hotel.domain.HotelBrand;
import com.hotelbooking.hotel.domain.HotelStatus;
import com.hotelbooking.hotel.dto.CreateBranchRequest;
import com.hotelbooking.hotel.repository.HotelBranchRepository;
import com.hotelbooking.hotel.repository.HotelBrandRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/branches")
@RequiredArgsConstructor
public class HotelBranchController {

    private final HotelBrandRepository brandRepository;
    private final HotelBranchRepository branchRepository;

    @PostMapping
    public ResponseEntity<HotelBranch> createBranch(
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody CreateBranchRequest request
    ) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        HotelBrand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new IllegalArgumentException("Brand not found"));

        HotelBranch branch = HotelBranch.builder()
                .brand(brand)
                .name(request.getName())
                .city(request.getCity())
                .status(HotelStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(branchRepository.save(branch));
    }
}