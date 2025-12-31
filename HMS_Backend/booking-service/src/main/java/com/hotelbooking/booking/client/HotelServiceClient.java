package com.hotelbooking.booking.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class HotelServiceClient {

    private final RestTemplate restTemplate;

    public void releaseHold(String holdId) {
        restTemplate.postForEntity(
                "http://HOTEL-SERVICE/hotels/holds/{holdId}/release",
                null,
                Void.class,
                holdId
        );
    }
}