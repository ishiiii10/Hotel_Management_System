package com.hotelbooking.hotel.service.impl;



import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelbooking.hotel.domain.Hotel;
import com.hotelbooking.hotel.dto.request.CreateHotelRequest;
import com.hotelbooking.hotel.dto.response.HotelDetailResponse;
import com.hotelbooking.hotel.dto.response.HotelSearchResponse;
import com.hotelbooking.hotel.enums.City;
import com.hotelbooking.hotel.enums.HotelStatus;
import com.hotelbooking.hotel.enums.Hotel_Category;
import com.hotelbooking.hotel.repository.HotelRepository;

import com.hotelbooking.hotel.service.HotelService;
import com.hotelbooking.hotel.service.RoomAvailabilityService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final RoomAvailabilityService availabilityService;

    @Override
    public Long createHotel(CreateHotelRequest request) {

        Hotel hotel = Hotel.builder()
                .name(request.getName())
                .category(request.getCategory())
                .description(request.getDescription())
                .address(request.getAddress())
                .city(request.getCity())          // enum
                .state(request.getState())        // enum
                .country(request.getCountry())
                .pincode(request.getPincode())
                .contactNumber(request.getContactNumber())
                .email(request.getEmail())
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
        hotel.setCategory(request.getCategory());
        hotel.setDescription(request.getDescription());
        hotel.setAddress(request.getAddress());
        hotel.setCity(request.getCity());
        hotel.setState(request.getState());
        hotel.setCountry(request.getCountry());
        hotel.setPincode(request.getPincode());
        hotel.setContactNumber(request.getContactNumber());
        hotel.setEmail(request.getEmail());
        
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
    public List<HotelSearchResponse> searchHotels(
            City city,
            LocalDate checkIn,
            LocalDate checkOut,
            Hotel_Category category
    ) {

        List<Hotel> hotels = (category != null)
                ? hotelRepository.findByCityAndCategoryAndStatus(
                        city, category, HotelStatus.ACTIVE)
                : hotelRepository.findByCityAndStatus(
                        city, HotelStatus.ACTIVE);

        return hotels.stream()
                .map(hotel -> {

                    int availableRooms;

                    if (checkIn != null && checkOut != null) {
                        availableRooms =
                                availabilityService
                                        .searchAvailability(
                                                hotel.getId(),
                                                checkIn,
                                                checkOut
                                        )
                                        .getAvailableRooms();
                    } else {
                        availableRooms = hotel.getAvailableRooms();
                    }

                    if (availableRooms == 0) {
                        return null;
                    }

                    return new HotelSearchResponse(
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
                            availableRooms
                    );
                })
                .filter(Objects::nonNull)
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
                .findByCategoryAndStatus(category, HotelStatus.ACTIVE)
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

	@Override
	public List<HotelSearchResponse> searchHotelsByCity(City city) {
		// TODO Auto-generated method stub
		return null;
	}
    
    }