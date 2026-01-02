package com.hotelbooking.hotel.util;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.hotelbooking.hotel.domain.RoomAvailability;
import com.hotelbooking.hotel.domain.AvailabilityStatus;
import com.hotelbooking.hotel.repository.RoomAvailabilityRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RoomAvailabilityGenerator {

    private final RoomAvailabilityRepository availabilityRepository;

    @Value("${hotel.availability.default-days}")
    private int defaultDays;

    @Transactional
    public void generateForRoom(Long hotelId, Long roomId) {

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(defaultDays);

        for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {

            boolean exists = availabilityRepository.existsByRoomIdAndDate(roomId, date);
            if (exists) {
                continue;
            }

            RoomAvailability availability = RoomAvailability.builder()
                    .hotelId(hotelId)
                    .roomId(roomId)
                    .date(date)
                    .status(AvailabilityStatus.AVAILABLE)
                    .source("SYSTEM")
                    .build();

            availabilityRepository.save(availability);
        }
    }
}