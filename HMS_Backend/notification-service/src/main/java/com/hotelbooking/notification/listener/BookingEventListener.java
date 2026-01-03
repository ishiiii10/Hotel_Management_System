package com.hotelbooking.notification.listener;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hotelbooking.notification.dto.BookingCancelledEvent;
import com.hotelbooking.notification.dto.BookingConfirmedEvent;
import com.hotelbooking.notification.dto.BookingCreatedEvent;
import com.hotelbooking.notification.dto.CheckoutCompletedEvent;
import com.hotelbooking.notification.dto.GuestCheckedInEvent;
import com.hotelbooking.notification.service.NotificationService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BookingEventListener {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public BookingEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @KafkaListener(topics = "booking-created", groupId = "notification-group")
    public void handleBookingCreated(ConsumerRecord<String, Object> record) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) record.value();
            log.info("Received booking-created event: {}", payload);
            BookingCreatedEvent event = objectMapper.convertValue(payload, BookingCreatedEvent.class);
            notificationService.handleBookingCreated(event);
        } catch (Exception e) {
            log.error("Error deserializing booking-created event: {}", record.value(), e);
        }
    }

    @KafkaListener(topics = "booking-confirmed", groupId = "notification-group")
    public void handleBookingConfirmed(ConsumerRecord<String, Object> record) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) record.value();
            log.info("Received booking-confirmed event: {}", payload);
            BookingConfirmedEvent event = objectMapper.convertValue(payload, BookingConfirmedEvent.class);
            notificationService.handleBookingConfirmed(event);
        } catch (Exception e) {
            log.error("Error deserializing booking-confirmed event: {}", record.value(), e);
        }
    }

    @KafkaListener(topics = "booking-cancelled", groupId = "notification-group")
    public void handleBookingCancelled(ConsumerRecord<String, Object> record) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) record.value();
            log.info("Received booking-cancelled event: {}", payload);
            BookingCancelledEvent event = objectMapper.convertValue(payload, BookingCancelledEvent.class);
            notificationService.handleBookingCancelled(event);
        } catch (Exception e) {
            log.error("Error deserializing booking-cancelled event: {}", record.value(), e);
        }
    }

    @KafkaListener(topics = "guest-checked-in", groupId = "notification-group")
    public void handleGuestCheckedIn(ConsumerRecord<String, Object> record) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) record.value();
            log.info("Received guest-checked-in event: {}", payload);
            GuestCheckedInEvent event = objectMapper.convertValue(payload, GuestCheckedInEvent.class);
            notificationService.handleGuestCheckedIn(event);
        } catch (Exception e) {
            log.error("Error deserializing guest-checked-in event: {}", record.value(), e);
        }
    }

    @KafkaListener(topics = "checkout-completed", groupId = "notification-group")
    public void handleCheckoutCompleted(ConsumerRecord<String, Object> record) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) record.value();
            log.info("Received checkout-completed event: {}", payload);
            CheckoutCompletedEvent event = objectMapper.convertValue(payload, CheckoutCompletedEvent.class);
            notificationService.handleCheckoutCompleted(event);
        } catch (Exception e) {
            log.error("Error deserializing checkout-completed event: {}", record.value(), e);
        }
    }
}
