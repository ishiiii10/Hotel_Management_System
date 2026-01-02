package com.hotelbooking.notification.service;

import org.springframework.stereotype.Service;

import com.hotelbooking.notification.dto.BookingCheckedInEvent;
import com.hotelbooking.notification.dto.BookingCompletedEvent;
import com.hotelbooking.notification.dto.BookingCreatedEvent;
import com.hotelbooking.notification.dto.HotelInfoResponse;
import com.hotelbooking.notification.dto.UserInfoResponse;
import com.hotelbooking.notification.feign.AuthServiceClient;
import com.hotelbooking.notification.feign.HotelServiceClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmailService emailService;
    private final SmsService smsService;
    private final AuthServiceClient authServiceClient;
    private final HotelServiceClient hotelServiceClient;

    public void handleBookingCreated(BookingCreatedEvent event) {
        try {
            log.info("Processing booking-created event for bookingId: {}", event.getBookingId());

            // Fetch user details
            UserInfoResponse user = authServiceClient.getUserById(event.getUserId());
            if (user == null || user.getEmail() == null) {
                log.warn("User not found or email missing for userId: {}", event.getUserId());
                return;
            }

            // Fetch hotel details
            HotelInfoResponse hotel = hotelServiceClient.getHotelById(event.getHotelId());
            if (hotel == null) {
                log.warn("Hotel not found for hotelId: {}", event.getHotelId());
                return;
            }

            // Send email notification
            String guestName = user.getFullName() != null ? user.getFullName() : user.getUsername();
            emailService.sendBookingConfirmationEmail(
                user.getEmail(),
                guestName,
                hotel.getName(),
                event.getCheckInDate(),
                event.getCheckOutDate(),
                event.getAmount()
            );

            // Send SMS notification (if phone number available)
            if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
                smsService.sendBookingConfirmationSms(
                    user.getPhoneNumber(),
                    hotel.getName(),
                    event.getCheckInDate(),
                    event.getCheckOutDate()
                );
            }

            log.info("Notifications sent successfully for bookingId: {}", event.getBookingId());
        } catch (Exception e) {
            log.error("Error processing booking-created event for bookingId: {}", 
                     event.getBookingId(), e);
            // Don't throw - we don't want to break the Kafka consumer
        }
    }

    public void handleBookingCheckedIn(BookingCheckedInEvent event) {
        try {
            log.info("Processing booking-checked-in event for bookingId: {}", event.getBookingId());

            // Fetch user details
            UserInfoResponse user = authServiceClient.getUserById(event.getUserId());
            if (user == null || user.getEmail() == null) {
                log.warn("User not found or email missing for userId: {}", event.getUserId());
                return;
            }

            // Fetch hotel details
            HotelInfoResponse hotel = hotelServiceClient.getHotelById(event.getHotelId());
            if (hotel == null) {
                log.warn("Hotel not found for hotelId: {}", event.getHotelId());
                return;
            }

            // Send email notification
            String guestName = user.getFullName() != null ? user.getFullName() : user.getUsername();
            emailService.sendCheckInNotificationEmail(
                user.getEmail(),
                guestName,
                hotel.getName(),
                java.time.LocalDate.now().toString()
            );

            // Send SMS notification
            if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
                smsService.sendCheckInNotificationSms(
                    user.getPhoneNumber(),
                    hotel.getName()
                );
            }

            log.info("Check-in notifications sent successfully for bookingId: {}", event.getBookingId());
        } catch (Exception e) {
            log.error("Error processing booking-checked-in event for bookingId: {}", 
                     event.getBookingId(), e);
        }
    }

    public void handleBookingCompleted(BookingCompletedEvent event) {
        try {
            log.info("Processing booking-completed event for bookingId: {}", event.getBookingId());

            // Fetch user details
            UserInfoResponse user = authServiceClient.getUserById(event.getUserId());
            if (user == null || user.getEmail() == null) {
                log.warn("User not found or email missing for userId: {}", event.getUserId());
                return;
            }

            // Fetch hotel details
            HotelInfoResponse hotel = hotelServiceClient.getHotelById(event.getHotelId());
            if (hotel == null) {
                log.warn("Hotel not found for hotelId: {}", event.getHotelId());
                return;
            }

            // Send email notification
            String guestName = user.getFullName() != null ? user.getFullName() : user.getUsername();
            emailService.sendBookingCompletedEmail(
                user.getEmail(),
                guestName,
                hotel.getName()
            );

            log.info("Booking completed notifications sent successfully for bookingId: {}", 
                    event.getBookingId());
        } catch (Exception e) {
            log.error("Error processing booking-completed event for bookingId: {}", 
                     event.getBookingId(), e);
        }
    }
}

