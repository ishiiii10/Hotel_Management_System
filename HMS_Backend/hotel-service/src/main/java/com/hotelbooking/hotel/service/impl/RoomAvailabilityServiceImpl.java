package com.hotelbooking.hotel.service.impl;


import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelbooking.hotel.domain.RoomAvailability;
import com.hotelbooking.hotel.repository.RoomAvailabilityRepository;
import com.hotelbooking.hotel.service.RoomAvailabilityService;
import com.hotelbooking.hotel.dto.request.BlockRoomRequest;
import com.hotelbooking.hotel.dto.request.UnblockRoomRequest;
import com.hotelbooking.hotel.dto.response.AvailabilitySearchResponse;
import com.hotelbooking.hotel.enums.AvailabilityStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomAvailabilityServiceImpl implements RoomAvailabilityService {

    private final RoomAvailabilityRepository availabilityRepository;

    @Override
    public void blockRoom(BlockRoomRequest request) {

        if (request.getFromDate().isAfter(request.getToDate())) {
            throw new IllegalArgumentException("From date cannot be after To date");
        }

        LocalDate date = request.getFromDate();

        while (!date.isAfter(request.getToDate())) {

            RoomAvailability availability =
                    availabilityRepository
                            .findByRoomIdAndDate(request.getRoomId(), date)
                            .orElse(
                                    RoomAvailability.builder()
                                            .hotelId(request.getHotelId())
                                            .roomId(request.getRoomId())
                                            .date(date)
                                            .build()
                            );

            availability.setStatus(AvailabilityStatus.BLOCKED);
            availability.setSource("MANUAL: " + request.getReason());

            availabilityRepository.save(availability);

            date = date.plusDays(1);
        }
    }
    
    @Override
    public void unblockRoom(UnblockRoomRequest request) {

        if (request.getFromDate().isAfter(request.getToDate())) {
            throw new IllegalArgumentException("From date cannot be after To date");
        }

        LocalDate date = request.getFromDate();

        while (!date.isAfter(request.getToDate())) {

            availabilityRepository
                    .findByRoomIdAndDate(request.getRoomId(), date)
                    .ifPresent(availability -> {

                        // IMPORTANT: Do NOT override RESERVED
                        if (availability.getStatus() == AvailabilityStatus.BLOCKED) {
                            availability.setStatus(AvailabilityStatus.AVAILABLE);
                            availability.setSource("SYSTEM");
                            availabilityRepository.save(availability);
                        }
                    });

            date = date.plusDays(1);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public AvailabilitySearchResponse searchAvailability(
            Long hotelId,
            LocalDate checkIn,
            LocalDate checkOut
    ) {

        if (!checkIn.isBefore(checkOut)) {
            throw new IllegalArgumentException("Check-in must be before check-out");
        }

        var roomIds = availabilityRepository.findAvailableRoomIds(
                hotelId,
                checkIn,
                checkOut,
                AvailabilityStatus.AVAILABLE
        );

        return new AvailabilitySearchResponse(
                hotelId,
                roomIds.size(),
                roomIds
        );
    }

}
