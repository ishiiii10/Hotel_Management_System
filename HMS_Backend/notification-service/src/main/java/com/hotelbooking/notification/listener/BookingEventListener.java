package com.hotelbooking.notification.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.hotelbooking.notification.dto.BookingCheckedInEvent;
import com.hotelbooking.notification.dto.BookingCompletedEvent;
import com.hotelbooking.notification.dto.BookingCreatedEvent;
import com.hotelbooking.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = "booking-created", groupId = "notification-group")
    public void handleBookingCreated(BookingCreatedEvent event) {
        log.info("Received booking-created event: {}", event);
        notificationService.handleBookingCreated(event);
    }

    @KafkaListener(topics = "booking-checked-in", groupId = "notification-group")
    public void handleBookingCheckedIn(BookingCheckedInEvent event) {
        log.info("Received booking-checked-in event: {}", event);
        notificationService.handleBookingCheckedIn(event);
    }

    @KafkaListener(topics = "booking-completed", groupId = "notification-group")
    public void handleBookingCompleted(BookingCompletedEvent event) {
        log.info("Received booking-completed event: {}", event);
        notificationService.handleBookingCompleted(event);
    }
}

