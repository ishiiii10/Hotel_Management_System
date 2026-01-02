package com.hotelbooking.notification.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.hotelbooking.notification.dto.HotelInfoResponse;

@FeignClient(name = "HOTEL-SERVICE", path = "/internal/hotels")
public interface HotelServiceClient {

    @GetMapping("/{hotelId}")
    HotelInfoResponse getHotelById(@PathVariable Long hotelId);
}

