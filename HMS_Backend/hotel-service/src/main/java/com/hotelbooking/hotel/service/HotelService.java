package com.hotelbooking.hotel.service;


import java.util.List;

import org.springframework.stereotype.Service;

import com.hotelbooking.hotel.domain.City;
import com.hotelbooking.hotel.domain.Hotel;
import com.hotelbooking.hotel.domain.HotelStatus;
import com.hotelbooking.hotel.dto.CreateHotelRequest;
import com.hotelbooking.hotel.dto.UpdateHotelRequest;
import com.hotelbooking.hotel.repository.HotelRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;

    public Hotel createHotel(CreateHotelRequest request) {
        Hotel hotel = Hotel.builder()
                .name(request.getName())
                .city(request.getCity())
                .address(request.getAddress())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .category(request.getCategory())
                .status(HotelStatus.ACTIVE)
                .build();

        return hotelRepository.save(hotel);
    }

    public Hotel updateHotel(Long hotelId, UpdateHotelRequest request) {
        Hotel hotel = getHotelOrThrow(hotelId);

        hotel.setName(request.getName());
        hotel.setAddress(request.getAddress());
        hotel.setEmail(request.getEmail());
        hotel.setPhoneNumber(request.getPhoneNumber());
        hotel.setCategory(request.getCategory());

        return hotelRepository.save(hotel);
    }

    public void disableHotel(Long hotelId) {
        Hotel hotel = getHotelOrThrow(hotelId);
        hotel.setStatus(HotelStatus.INACTIVE);
        hotelRepository.save(hotel);
    }

    public Hotel getHotel(Long hotelId) {
        return getHotelOrThrow(hotelId);
    }

    public List<Hotel> listHotels(City city) {
        return city != null
                ? hotelRepository.findByCity(city)
                : hotelRepository.findAll();
    }

    private Hotel getHotelOrThrow(Long hotelId) {
        return hotelRepository.findById(hotelId)
                .orElseThrow(() -> new IllegalStateException("Hotel not found"));
    }
}