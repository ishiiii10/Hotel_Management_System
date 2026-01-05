package com.hotelbooking.billing.listener;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hotelbooking.billing.dto.BookingConfirmedEvent;
import com.hotelbooking.billing.dto.BookingCreatedEvent;
import com.hotelbooking.billing.service.BillingService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BookingEventListener {

    private final BillingService billingService;
    private final ObjectMapper objectMapper;

    public BookingEventListener(BillingService billingService) {
        this.billingService = billingService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Listen to booking-created events to generate bills for walk-in bookings immediately
     */
    @KafkaListener(topics = "booking-created", groupId = "billing-group")
    public void handleBookingCreated(ConsumerRecord<String, Object> record) {
        try {
            log.info("Kafka listener triggered for booking-created topic. Key: {}, Value type: {}", 
                    record.key(), record.value() != null ? record.value().getClass().getName() : "null");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) record.value();
            log.info("Received booking-created event: {}", payload);
            
            BookingCreatedEvent event = objectMapper.convertValue(payload, BookingCreatedEvent.class);
            log.info("Converted to BookingCreatedEvent. bookingId: {}, source: {}", 
                    event.getBookingId(), event.getBookingSource());
            
            // Generate bill immediately for all bookings (both PUBLIC and WALK_IN)
            billingService.generateBillForCreatedBooking(event);
            log.info("Bill generation for booking completed for bookingId: {}", event.getBookingId());
        } catch (Exception e) {
            log.error("Error processing booking-created event. Key: {}, Value: {}", 
                    record.key(), record.value(), e);
        }
    }

    /**
     * Listen to booking-confirmed events to generate bills for PUBLIC bookings
     */
    @KafkaListener(topics = "booking-confirmed", groupId = "billing-group")
    public void handleBookingConfirmed(ConsumerRecord<String, Object> record) {
        try {
            log.info("Kafka listener triggered for booking-confirmed topic. Key: {}, Value type: {}", 
                    record.key(), record.value() != null ? record.value().getClass().getName() : "null");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) record.value();
            log.info("Received booking-confirmed event: {}", payload);
            
            BookingConfirmedEvent event = objectMapper.convertValue(payload, BookingConfirmedEvent.class);
            log.info("Converted to BookingConfirmedEvent. bookingId: {}", event.getBookingId());
            
            billingService.generateBill(event);
            log.info("Bill generation completed for bookingId: {}", event.getBookingId());
        } catch (Exception e) {
            log.error("Error processing booking-confirmed event. Key: {}, Value: {}", 
                    record.key(), record.value(), e);
        }
    }
}

