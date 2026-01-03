package com.hotelbooking.billing.listener;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hotelbooking.billing.dto.BookingConfirmedEvent;
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

    @KafkaListener(topics = "booking-confirmed", groupId = "billing-group")
    public void handleBookingConfirmed(ConsumerRecord<String, Object> record) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) record.value();
            log.info("Received booking-confirmed event: {}", payload);
            BookingConfirmedEvent event = objectMapper.convertValue(payload, BookingConfirmedEvent.class);
            billingService.generateBill(event);
        } catch (Exception e) {
            log.error("Error processing booking-confirmed event: {}", record.value(), e);
        }
    }
}

