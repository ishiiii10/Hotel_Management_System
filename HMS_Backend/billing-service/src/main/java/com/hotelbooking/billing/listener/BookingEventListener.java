package com.hotelbooking.billing.listener;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hotelbooking.billing.dto.BookingConfirmedEvent;
import com.hotelbooking.billing.dto.BookingCreatedEvent;
import com.hotelbooking.billing.exception.BillGenerationException;
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
        String recordKey = record != null ? record.key() : "null";
        Object recordValue = record != null ? record.value() : null;
        
        try {
            if (record == null) {
                log.warn("Received null ConsumerRecord for booking-created topic");
                return;
            }
            
            if (recordValue == null) {
                log.warn("Received null value in booking-created event. Key: {}", recordKey);
                return;
            }
            
            log.info("Kafka listener triggered for booking-created topic. Key: {}, Value type: {}", 
                    recordKey, recordValue.getClass().getName());
            
            if (!(recordValue instanceof Map)) {
                log.error("Invalid payload type for booking-created event. Expected Map, got: {}", 
                        recordValue.getClass().getName());
                return;
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) recordValue;
            log.info("Received booking-created event: {}", payload);
            
            BookingCreatedEvent event = objectMapper.convertValue(payload, BookingCreatedEvent.class);
            
            if (event == null || event.getBookingId() == null) {
                log.error("Invalid BookingCreatedEvent: missing bookingId. Payload: {}", payload);
                return;
            }
            
            log.info("Converted to BookingCreatedEvent. bookingId: {}, source: {}", 
                    event.getBookingId(), event.getBookingSource());
            
            // Generate bill immediately for all bookings (both PUBLIC and WALK_IN)
            billingService.generateBillForCreatedBooking(event);
            log.info("Bill generation for booking completed for bookingId: {}", event.getBookingId());
        } catch (BillGenerationException e) {
            log.error("Bill generation failed for booking-created event. Key: {}, Value: {}", 
                    recordKey, recordValue, e);
            // Don't rethrow - allow processing to continue for other events
        } catch (IllegalArgumentException e) {
            log.error("Invalid payload format for booking-created event. Key: {}, Value: {}", 
                    recordKey, recordValue, e);
        } catch (Exception e) {
            log.error("Unexpected error processing booking-created event. Key: {}, Value: {}", 
                    recordKey, recordValue, e);
        }
    }

    /**
     * Listen to booking-confirmed events to generate bills for PUBLIC bookings
     */
    @KafkaListener(topics = "booking-confirmed", groupId = "billing-group")
    public void handleBookingConfirmed(ConsumerRecord<String, Object> record) {
        String recordKey = record != null ? record.key() : "null";
        Object recordValue = record != null ? record.value() : null;
        
        try {
            if (record == null) {
                log.warn("Received null ConsumerRecord for booking-confirmed topic");
                return;
            }
            
            if (recordValue == null) {
                log.warn("Received null value in booking-confirmed event. Key: {}", recordKey);
                return;
            }
            
            log.info("Kafka listener triggered for booking-confirmed topic. Key: {}, Value type: {}", 
                    recordKey, recordValue.getClass().getName());
            
            if (!(recordValue instanceof Map)) {
                log.error("Invalid payload type for booking-confirmed event. Expected Map, got: {}", 
                        recordValue.getClass().getName());
                return;
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) recordValue;
            log.info("Received booking-confirmed event: {}", payload);
            
            BookingConfirmedEvent event = objectMapper.convertValue(payload, BookingConfirmedEvent.class);
            
            if (event == null || event.getBookingId() == null) {
                log.error("Invalid BookingConfirmedEvent: missing bookingId. Payload: {}", payload);
                return;
            }
            
            log.info("Converted to BookingConfirmedEvent. bookingId: {}", event.getBookingId());
            
            billingService.generateBill(event);
            log.info("Bill generation completed for bookingId: {}", event.getBookingId());
        } catch (BillGenerationException e) {
            log.error("Bill generation failed for booking-confirmed event. Key: {}, Value: {}", 
                    recordKey, recordValue, e);
            // Don't rethrow - allow processing to continue for other events
        } catch (IllegalArgumentException e) {
            log.error("Invalid payload format for booking-confirmed event. Key: {}, Value: {}", 
                    recordKey, recordValue, e);
        } catch (Exception e) {
            log.error("Unexpected error processing booking-confirmed event. Key: {}, Value: {}", 
                    recordKey, recordValue, e);
        }
    }
}

