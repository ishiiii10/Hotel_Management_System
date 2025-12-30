package com.hotelbooking.hotel.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.hotelbooking.hotel.domain.City;
import com.hotelbooking.hotel.domain.Hotel;
import com.hotelbooking.hotel.domain.HotelStatus;
import com.hotelbooking.hotel.domain.RoomCategory;
import com.hotelbooking.hotel.dto.PublicHotelSearchResponse;
import com.hotelbooking.hotel.repository.HotelRepository;
import com.hotelbooking.hotel.repository.RoomCategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PublicHotelSearchService {

    private final HotelRepository hotelRepository;
    private final RoomCategoryRepository categoryRepository;
    private final AvailabilityService availabilityService;

    public List<PublicHotelSearchResponse> search(
            City city,
            LocalDate checkIn,
            LocalDate checkOut
    ) {
        List<Hotel> hotels = hotelRepository.findByCityAndStatus(city, HotelStatus.ACTIVE);

        return hotels.stream()
                .map(hotel -> {

                    List<PublicHotelSearchResponse.AvailableCategory> categories =
                            categoryRepository.findByHotelId(hotel.getId())
                                    .stream()
                                    .map(category -> {
                                        int available =
                                                availabilityService.getAvailability(
                                                        hotel.getId(),
                                                        category.getId(),
                                                        checkIn,
                                                        checkOut
                                                );

                                        if (available <= 0) return null;

                                        return new PublicHotelSearchResponse.AvailableCategory(
                                                category.getId(),
                                                category.getName(),
                                                available,
                                                category.getBasePrice()
                                        );
                                    })
                                    .filter(c -> c != null)
                                    .toList();

                    if (categories.isEmpty()) return null;

                    return new PublicHotelSearchResponse(
                            hotel.getId(),
                            hotel.getName(),
                            hotel.getCity(),
                            hotel.getCategory(),
                            categories
                    );
                })
                .filter(h -> h != null)
                .toList();
    }
}