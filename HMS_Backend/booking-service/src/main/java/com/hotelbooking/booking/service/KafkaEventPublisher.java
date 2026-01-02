package com.hotelbooking.booking.service;

import java.util.Map;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishBookingCreated(Long bookingId, Long userId, Long hotelId, Long roomId, 
                                     String checkInDate, String checkOutDate, Double amount) {
        Map<String, Object> event = Map.of(
                "bookingId", bookingId,
                "userId", userId,
                "hotelId", hotelId,
                "roomId", roomId,
                "checkInDate", checkInDate,
                "checkOutDate", checkOutDate,
                "amount", amount
        );
        kafkaTemplate.send("booking-created", String.valueOf(bookingId), event);
        log.info("Published booking-created event for bookingId: {}", bookingId);
    }

    public void publishBookingCheckedIn(Long bookingId, Long userId, Long hotelId) {
        Map<String, Object> event = Map.of(
                "bookingId", bookingId,
                "userId", userId,
                "hotelId", hotelId
        );
        kafkaTemplate.send("booking-checked-in", String.valueOf(bookingId), event);
        log.info("Published booking-checked-in event for bookingId: {}", bookingId);
    }

    public void publishBookingCompleted(Long bookingId, Long userId, Long hotelId) {
        Map<String, Object> event = Map.of(
                "bookingId", bookingId,
                "userId", userId,
                "hotelId", hotelId
        );
        kafkaTemplate.send("booking-completed", String.valueOf(bookingId), event);
        log.info("Published booking-completed event for bookingId: {}", bookingId);
    }
}

