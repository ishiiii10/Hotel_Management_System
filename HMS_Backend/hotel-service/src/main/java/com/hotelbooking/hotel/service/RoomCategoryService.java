package com.hotelbooking.hotel.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hotelbooking.hotel.domain.RoomCategory;
import com.hotelbooking.hotel.repository.RoomCategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomCategoryService {

    private final RoomCategoryRepository repository;

    public RoomCategory create(Long hotelId, RoomCategory category) {
        category.setHotelId(hotelId);
        return repository.save(category);
    }

    public RoomCategory update(RoomCategory category) {
        return repository.save(category);
    }

    public RoomCategory get(Long categoryId) {
        return repository.findById(categoryId)
                .orElseThrow(() -> new IllegalStateException("Category not found"));
    }

    public List<RoomCategory> listByHotel(Long hotelId) {
        return repository.findByHotelId(hotelId);
    }
}