package com.hotelbooking.notification.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SmsService {

    @Value("${notification.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${notification.sms.provider:mock}")
    private String smsProvider;

    public void sendSms(String phoneNumber, String message) {
        if (!smsEnabled) {
            log.info("SMS service is disabled. Would send SMS to {}: {}", phoneNumber, message);
            return;
        }

        try {
            // TODO: Integrate with actual SMS provider (Twilio, AWS SNS, etc.)
            if ("mock".equals(smsProvider)) {
                log.info("Mock SMS sent to {}: {}", phoneNumber, message);
            } else {
                // Implement actual SMS sending logic here
                log.info("SMS sent to {} via {}: {}", phoneNumber, smsProvider, message);
            }
        } catch (Exception e) {
            log.error("Failed to send SMS to: {}", phoneNumber, e);
            // Don't throw exception - SMS failures shouldn't break the flow
        }
    }

    public void sendBookingConfirmationSms(String phoneNumber, String hotelName, 
                                          String checkInDate, String checkOutDate) {
        String message = String.format(
            "Your booking at %s is confirmed. Check-in: %s, Check-out: %s. Thank you!",
            hotelName, checkInDate, checkOutDate
        );
        sendSms(phoneNumber, message);
    }

    public void sendCheckInNotificationSms(String phoneNumber, String hotelName) {
        String message = String.format(
            "Welcome to %s! Your check-in is confirmed. We hope you have a pleasant stay!",
            hotelName
        );
        sendSms(phoneNumber, message);
    }
}

