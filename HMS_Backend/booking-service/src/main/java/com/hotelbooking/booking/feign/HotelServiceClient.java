package com.hotelbooking.booking.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.hotelbooking.booking.dto.response.HotelDetailResponse;
import com.hotelbooking.booking.dto.response.RoomResponse;

@FeignClient(name = "HOTEL-SERVICE", path = "/internal/hotels")
public interface HotelServiceClient {

    @GetMapping("/{hotelId}")
    HotelDetailResponse getHotelById(@PathVariable Long hotelId);

    @GetMapping("/{hotelId}/rooms")
    List<RoomResponse> getRoomsByHotel(@PathVariable Long hotelId);

    @GetMapping("/rooms/{roomId}")
    RoomResponse getRoomById(@PathVariable Long roomId);
}

