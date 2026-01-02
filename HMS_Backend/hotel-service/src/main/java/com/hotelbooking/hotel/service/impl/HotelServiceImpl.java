package com.hotelbooking.hotel.service.impl;



import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelbooking.hotel.domain.City;
import com.hotelbooking.hotel.domain.Hotel;
import com.hotelbooking.hotel.domain.HotelStatus;
import com.hotelbooking.hotel.domain.Hotel_Category;
import com.hotelbooking.hotel.dto.CreateHotelRequest;
import com.hotelbooking.hotel.dto.HotelDetailResponse;
import com.hotelbooking.hotel.dto.HotelSearchResponse;
import com.hotelbooking.hotel.repository.HotelRepository;
import com.hotelbooking.hotel.service.HotelService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;

    @Override
    public Long createHotel(CreateHotelRequest request) {

        Hotel hotel = Hotel.builder()
                .name(request.getName())
                .description(request.getDescription())
                .address(request.getAddress())
                .city(request.getCity())          // enum
                .state(request.getState())        // enum
                .country(request.getCountry())
                .pincode(request.getPincode())
                .contactNumber(request.getContactNumber())
                .email(request.getEmail())
                .category(request.getCategory()) // enum
                .starRating(request.getStarRating())
                .amenities(request.getAmenities())
                .status(request.getStatus())
                .totalRooms(request.getTotalRooms())
                .availableRooms(request.getTotalRooms())
                .imageUrl(request.getImageUrl())
                .build();

        return hotelRepository.save(hotel).getId();
    }

    @Override
    public Long updateHotel(Long hotelId, CreateHotelRequest request) {

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new IllegalStateException("Hotel not found"));

        hotel.setName(request.getName());
        hotel.setDescription(request.getDescription());
        hotel.setAddress(request.getAddress());
        hotel.setCity(request.getCity());
        hotel.setState(request.getState());
        hotel.setCountry(request.getCountry());
        hotel.setPincode(request.getPincode());
        hotel.setContactNumber(request.getContactNumber());
        hotel.setEmail(request.getEmail());
        hotel.setCategory(request.getCategory());
        hotel.setStarRating(request.getStarRating());
        hotel.setAmenities(request.getAmenities());
        hotel.setStatus(request.getStatus());
        hotel.setImageUrl(request.getImageUrl());

        hotelRepository.save(hotel);

        return hotel.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public HotelDetailResponse getHotelById(Long hotelId) {

        Hotel h = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new IllegalStateException("Hotel not found"));

        return new HotelDetailResponse(
                h.getId(),
                h.getName(),
                h.getCategory(),
                h.getDescription(),
                h.getAddress(),
                h.getCity(),
                h.getState(),
                h.getCountry(),
                h.getPincode(),
                h.getContactNumber(),
                h.getEmail(),
                h.getStarRating(),
                h.getAmenities(),
                h.getStatus(),
                h.getTotalRooms(),
                h.getAvailableRooms(),
                h.getCreatedAt(),
                h.getUpdatedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelSearchResponse> searchHotelsByCity(City city) {

        return hotelRepository
                .findByCityIgnoreCaseAndStatus(city, HotelStatus.ACTIVE)
                .stream()
                .map(h -> new HotelSearchResponse(
                        h.getId(),
                        h.getName(),
                        h.getCategory(),
                        h.getDescription(),
                        h.getAddress(),
                        h.getCity(),
                        h.getState(),
                        h.getCountry(),
                        h.getPincode(),
                        h.getContactNumber(),
                        h.getEmail(),
                        h.getStarRating(),
                        h.getAmenities(),
                        h.getImageUrl(),
                        h.getStatus(),
                        h.getTotalRooms(),
                        h.getAvailableRooms()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelDetailResponse> getAllHotels() {

        return hotelRepository.findAll()
                .stream()
                .map(h -> new HotelDetailResponse(
                        h.getId(),
                        h.getName(),
                        h.getCategory(),
                        h.getDescription(),
                        h.getAddress(),
                        h.getCity(),
                        h.getState(),
                        h.getCountry(),
                        h.getPincode(),
                        h.getContactNumber(),
                        h.getEmail(),
                        h.getStarRating(),
                        h.getAmenities(),
                        h.getStatus(),
                        h.getTotalRooms(),
                        h.getAvailableRooms(),
                        h.getCreatedAt(),
                        h.getUpdatedAt()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelSearchResponse> searchHotelsByCategory(Hotel_Category category) {

        return hotelRepository
                .findByHotelCategoryAndStatus(category, HotelStatus.ACTIVE)
                .stream()
                .map(hotel -> new HotelSearchResponse(
                        hotel.getId(),
                        hotel.getName(),
                        hotel.getCategory(),
                        hotel.getDescription(),
                        hotel.getAddress(),
                        hotel.getCity(),
                        hotel.getState(),
                        hotel.getCountry(),
                        hotel.getPincode(),
                        hotel.getContactNumber(),
                        hotel.getEmail(),
                        hotel.getStarRating(),
                        hotel.getAmenities(),
                        hotel.getImageUrl(),
                        hotel.getStatus(),
                        hotel.getTotalRooms(),
                        hotel.getAvailableRooms()
                ))
                .toList();
    }
	
	
}