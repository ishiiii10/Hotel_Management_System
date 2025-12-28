package com.hotelbooking.hotel.controller;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotelbooking.hotel.domain.HotelBrand;
import com.hotelbooking.hotel.repository.HotelBrandRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/brands")
@RequiredArgsConstructor
public class HotelBrandController {

    private final HotelBrandRepository brandRepository;

    @PostMapping
    public ResponseEntity<HotelBrand> createBrand(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") Long adminId,
            @Valid @RequestBody HotelBrand brand
    ) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        brand.setId(null);
        brand.setCreatedAt(LocalDateTime.now());
        brand.setCreatedByAdminId(adminId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(brandRepository.save(brand));
    }
}