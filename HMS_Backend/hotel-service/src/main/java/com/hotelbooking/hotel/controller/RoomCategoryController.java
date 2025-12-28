package com.hotelbooking.hotel.controller;



import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.hotelbooking.hotel.domain.RoomCategory;
import com.hotelbooking.hotel.repository.RoomCategoryRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RestController
@RequestMapping("/room-categories")
@RequiredArgsConstructor
public class RoomCategoryController {

    private final RoomCategoryRepository repository;

    @PostMapping
    public ResponseEntity<RoomCategory> create(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") Long adminId,
            @Valid @RequestBody RoomCategory category
    ) {
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        category.setId(null);
        category.setCreatedByAdminId(adminId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(repository.save(category));
    }

    @GetMapping
    public List<RoomCategory> list() {
        return repository.findAll();
    }
}