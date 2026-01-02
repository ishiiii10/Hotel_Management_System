package com.hotelbooking.hotel.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotelbooking.hotel.dto.response.HotelDetailResponse;
import com.hotelbooking.hotel.dto.response.RoomResponse;
import com.hotelbooking.hotel.service.HotelService;
import com.hotelbooking.hotel.service.RoomService;

import lombok.RequiredArgsConstructor;

/**
 * Internal controller for inter-service communication via Feign.
 * Returns raw data without wrapper objects for easier deserialization.
 */
@RestController
@RequestMapping("/internal/hotels")
@RequiredArgsConstructor
public class InternalHotelController {

    private final HotelService hotelService;
    private final RoomService roomService;

    @GetMapping("/{hotelId}")
    public HotelDetailResponse getHotelById(@PathVariable Long hotelId) {
        return hotelService.getHotelById(hotelId);
    }

    @GetMapping("/{hotelId}/rooms")
    public List<RoomResponse> getRoomsByHotel(@PathVariable Long hotelId) {
        return roomService.getRoomsByHotel(hotelId);
    }

    @GetMapping("/rooms/{roomId}")
    public RoomResponse getRoomById(@PathVariable Long roomId) {
        return roomService.getRoomById(roomId);
    }
}

