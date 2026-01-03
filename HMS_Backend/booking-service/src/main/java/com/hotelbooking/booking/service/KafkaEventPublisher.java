package com.hotelbooking.booking.service;

import java.time.LocalDateTime;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.hotelbooking.booking.event.BookingCancelledEvent;
import com.hotelbooking.booking.event.BookingConfirmedEvent;
import com.hotelbooking.booking.event.BookingCreatedEvent;
import com.hotelbooking.booking.event.CheckoutCompletedEvent;
import com.hotelbooking.booking.event.GuestCheckedInEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishBookingCreated(BookingCreatedEvent event) {
        try {
            kafkaTemplate.send("booking-created", String.valueOf(event.getBookingId()), event);
            log.info("Published BookingCreatedEvent for bookingId: {}", event.getBookingId());
        } catch (Exception e) {
            log.error("Failed to publish BookingCreatedEvent for bookingId: {}", 
                     event.getBookingId(), e);
        }
    }

    public void publishBookingConfirmed(BookingConfirmedEvent event) {
        try {
            kafkaTemplate.send("booking-confirmed", String.valueOf(event.getBookingId()), event);
            log.info("Published BookingConfirmedEvent for bookingId: {}", event.getBookingId());
        } catch (Exception e) {
            log.error("Failed to publish BookingConfirmedEvent for bookingId: {}", 
                     event.getBookingId(), e);
        }
    }

    public void publishBookingCancelled(BookingCancelledEvent event) {
        try {
            kafkaTemplate.send("booking-cancelled", String.valueOf(event.getBookingId()), event);
            log.info("Published BookingCancelledEvent for bookingId: {}", event.getBookingId());
        } catch (Exception e) {
            log.error("Failed to publish BookingCancelledEvent for bookingId: {}", 
                     event.getBookingId(), e);
        }
    }

    public void publishGuestCheckedIn(GuestCheckedInEvent event) {
        try {
            kafkaTemplate.send("guest-checked-in", String.valueOf(event.getBookingId()), event);
            log.info("Published GuestCheckedInEvent for bookingId: {}", event.getBookingId());
        } catch (Exception e) {
            log.error("Failed to publish GuestCheckedInEvent for bookingId: {}", 
                     event.getBookingId(), e);
        }
    }

    public void publishCheckoutCompleted(CheckoutCompletedEvent event) {
        try {
            kafkaTemplate.send("checkout-completed", String.valueOf(event.getBookingId()), event);
            log.info("Published CheckoutCompletedEvent for bookingId: {}", event.getBookingId());
        } catch (Exception e) {
            log.error("Failed to publish CheckoutCompletedEvent for bookingId: {}", 
                     event.getBookingId(), e);
        }
    }
}

