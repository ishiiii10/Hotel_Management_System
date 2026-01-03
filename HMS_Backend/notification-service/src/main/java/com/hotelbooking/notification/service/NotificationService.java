package com.hotelbooking.notification.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelbooking.notification.domain.ScheduledReminder;
import com.hotelbooking.notification.dto.BookingCancelledEvent;
import com.hotelbooking.notification.dto.BookingConfirmedEvent;
import com.hotelbooking.notification.dto.BookingCreatedEvent;
import com.hotelbooking.notification.dto.CheckoutCompletedEvent;
import com.hotelbooking.notification.dto.GuestCheckedInEvent;
import com.hotelbooking.notification.dto.HotelInfoResponse;
import com.hotelbooking.notification.feign.HotelServiceClient;
import com.hotelbooking.notification.repository.ScheduledReminderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmailService emailService;
    private final SmsService smsService;
    private final HotelServiceClient hotelServiceClient;
    private final ScheduledReminderRepository reminderRepository;

    @Value("${notification.feedback.base-url:http://localhost:3000/feedback}")
    private String feedbackBaseUrl;

    @Transactional
    public void handleBookingCreated(BookingCreatedEvent event) {
        try {
            log.info("Processing BookingCreatedEvent for bookingId: {}", event.getBookingId());

            // Fetch hotel details
            HotelInfoResponse hotel = hotelServiceClient.getHotelById(event.getHotelId());
            if (hotel == null) {
                log.warn("Hotel not found for hotelId: {}", event.getHotelId());
                return;
            }

            String guestName = event.getGuestName() != null ? event.getGuestName() : "Guest";
            String guestEmail = event.getGuestEmail();

            if (guestEmail == null || guestEmail.isEmpty()) {
                log.warn("Guest email missing for bookingId: {}", event.getBookingId());
                return;
            }

            // Send booking created email
            emailService.sendBookingConfirmationEmail(
                guestEmail,
                guestName,
                hotel.getName(),
                event.getCheckInDate().toString(),
                event.getCheckOutDate().toString(),
                event.getAmount()
            );

            // Schedule check-in reminder (24 hours before check-in)
            scheduleCheckInReminder(event, hotel);

            log.info("BookingCreatedEvent processed successfully for bookingId: {}", event.getBookingId());
        } catch (Exception e) {
            log.error("Error processing BookingCreatedEvent for bookingId: {}", 
                     event.getBookingId(), e);
        }
    }

    @Transactional
    public void handleBookingConfirmed(BookingConfirmedEvent event) {
        try {
            log.info("Processing BookingConfirmedEvent for bookingId: {}", event.getBookingId());

            // Fetch hotel details
            HotelInfoResponse hotel = hotelServiceClient.getHotelById(event.getHotelId());
            if (hotel == null) {
                log.warn("Hotel not found for hotelId: {}", event.getHotelId());
                return;
            }

            String guestName = event.getGuestName() != null ? event.getGuestName() : "Guest";
            String guestEmail = event.getGuestEmail();

            if (guestEmail == null || guestEmail.isEmpty()) {
                log.warn("Guest email missing for bookingId: {}", event.getBookingId());
                return;
            }

            // Send confirmation email
            emailService.sendBookingConfirmationEmail(
                guestEmail,
                guestName,
                hotel.getName(),
                event.getCheckInDate().toString(),
                event.getCheckOutDate().toString(),
                event.getAmount()
            );

            // Schedule check-in reminder if not already scheduled
            scheduleCheckInReminder(event, hotel);

            log.info("BookingConfirmedEvent processed successfully for bookingId: {}", event.getBookingId());
        } catch (Exception e) {
            log.error("Error processing BookingConfirmedEvent for bookingId: {}", 
                     event.getBookingId(), e);
        }
    }

    @Transactional
    public void handleBookingCancelled(BookingCancelledEvent event) {
        try {
            log.info("Processing BookingCancelledEvent for bookingId: {}", event.getBookingId());

            // Cancel scheduled reminders
            reminderRepository.findByBookingId(event.getBookingId())
                .forEach(reminder -> {
                    reminder.setCancelled(true);
                    reminderRepository.save(reminder);
                });

            // Fetch hotel details
            HotelInfoResponse hotel = hotelServiceClient.getHotelById(event.getHotelId());
            if (hotel == null) {
                log.warn("Hotel not found for hotelId: {}", event.getHotelId());
                return;
            }

            String guestName = event.getGuestName() != null ? event.getGuestName() : "Guest";
            String guestEmail = event.getGuestEmail();

            if (guestEmail == null || guestEmail.isEmpty()) {
                log.warn("Guest email missing for bookingId: {}", event.getBookingId());
                return;
            }

            // Send cancellation email
            emailService.sendCancellationEmail(
                guestEmail,
                guestName,
                hotel.getName(),
                event.getCheckInDate().toString(),
                event.getCancellationReason() != null ? event.getCancellationReason() : "Not specified"
            );

            log.info("BookingCancelledEvent processed successfully for bookingId: {}", event.getBookingId());
        } catch (Exception e) {
            log.error("Error processing BookingCancelledEvent for bookingId: {}", 
                     event.getBookingId(), e);
        }
    }

    @Transactional
    public void handleGuestCheckedIn(GuestCheckedInEvent event) {
        try {
            log.info("Processing GuestCheckedInEvent for bookingId: {}", event.getBookingId());

            // Fetch hotel details
            HotelInfoResponse hotel = hotelServiceClient.getHotelById(event.getHotelId());
            if (hotel == null) {
                log.warn("Hotel not found for hotelId: {}", event.getHotelId());
                return;
            }

            String guestName = event.getGuestName() != null ? event.getGuestName() : "Guest";
            String guestEmail = event.getGuestEmail();

            if (guestEmail == null || guestEmail.isEmpty()) {
                log.warn("Guest email missing for bookingId: {}", event.getBookingId());
                return;
            }

            // Send welcome email
            emailService.sendCheckInNotificationEmail(
                guestEmail,
                guestName,
                hotel.getName(),
                event.getCheckInDate().toString()
            );

            // Cancel any pending reminders (guest already checked in)
            reminderRepository.findByBookingId(event.getBookingId())
                .forEach(reminder -> {
                    reminder.setCancelled(true);
                    reminderRepository.save(reminder);
                });

            log.info("GuestCheckedInEvent processed successfully for bookingId: {}", event.getBookingId());
        } catch (Exception e) {
            log.error("Error processing GuestCheckedInEvent for bookingId: {}", 
                     event.getBookingId(), e);
        }
    }

    @Transactional
    public void handleCheckoutCompleted(CheckoutCompletedEvent event) {
        try {
            log.info("Processing CheckoutCompletedEvent for bookingId: {}", event.getBookingId());

            // Fetch hotel details
            HotelInfoResponse hotel = hotelServiceClient.getHotelById(event.getHotelId());
            if (hotel == null) {
                log.warn("Hotel not found for hotelId: {}", event.getHotelId());
                return;
            }

            String guestName = event.getGuestName() != null ? event.getGuestName() : "Guest";
            String guestEmail = event.getGuestEmail();

            if (guestEmail == null || guestEmail.isEmpty()) {
                log.warn("Guest email missing for bookingId: {}", event.getBookingId());
                return;
            }

            // Send thank-you email
            emailService.sendBookingCompletedEmail(
                guestEmail,
                guestName,
                hotel.getName()
            );

            // Generate feedback link (booking-scoped and time-limited)
            String feedbackToken = UUID.randomUUID().toString();
            String feedbackLink = String.format("%s?token=%s&bookingId=%d", 
                feedbackBaseUrl, feedbackToken, event.getBookingId());

            // Send feedback request email
            emailService.sendFeedbackRequestEmail(
                guestEmail,
                guestName,
                hotel.getName(),
                event.getBookingId(),
                feedbackLink
            );

            log.info("CheckoutCompletedEvent processed successfully for bookingId: {}", event.getBookingId());
        } catch (Exception e) {
            log.error("Error processing CheckoutCompletedEvent for bookingId: {}", 
                     event.getBookingId(), e);
        }
    }

    private void scheduleCheckInReminder(BookingCreatedEvent event, HotelInfoResponse hotel) {
        try {
            // Check if reminder already exists
            if (reminderRepository.findByBookingIdAndReminderType(
                    event.getBookingId(), "CHECK_IN_REMINDER").isPresent()) {
                log.debug("Check-in reminder already scheduled for bookingId: {}", event.getBookingId());
                return;
            }

            // Schedule reminder 24 hours before check-in
            LocalDate reminderDate = event.getCheckInDate().minusDays(1);

            ScheduledReminder reminder = ScheduledReminder.builder()
                .bookingId(event.getBookingId())
                .userId(event.getUserId())
                .hotelId(event.getHotelId())
                .reminderType("CHECK_IN_REMINDER")
                .scheduledDate(reminderDate)
                .checkInDate(event.getCheckInDate())
                .guestEmail(event.getGuestEmail())
                .guestName(event.getGuestName())
                .sent(false)
                .cancelled(false)
                .build();

            reminderRepository.save(reminder);
            log.info("Scheduled check-in reminder for bookingId: {} on date: {}", 
                    event.getBookingId(), reminderDate);
        } catch (Exception e) {
            log.error("Failed to schedule check-in reminder for bookingId: {}", 
                     event.getBookingId(), e);
        }
    }

    private void scheduleCheckInReminder(BookingConfirmedEvent event, HotelInfoResponse hotel) {
        try {
            // Check if reminder already exists
            if (reminderRepository.findByBookingIdAndReminderType(
                    event.getBookingId(), "CHECK_IN_REMINDER").isPresent()) {
                log.debug("Check-in reminder already scheduled for bookingId: {}", event.getBookingId());
                return;
            }

            // Schedule reminder 24 hours before check-in
            LocalDate reminderDate = event.getCheckInDate().minusDays(1);

            ScheduledReminder reminder = ScheduledReminder.builder()
                .bookingId(event.getBookingId())
                .userId(event.getUserId())
                .hotelId(event.getHotelId())
                .reminderType("CHECK_IN_REMINDER")
                .scheduledDate(reminderDate)
                .checkInDate(event.getCheckInDate())
                .guestEmail(event.getGuestEmail())
                .guestName(event.getGuestName())
                .sent(false)
                .cancelled(false)
                .build();

            reminderRepository.save(reminder);
            log.info("Scheduled check-in reminder for bookingId: {} on date: {}", 
                    event.getBookingId(), reminderDate);
        } catch (Exception e) {
            log.error("Failed to schedule check-in reminder for bookingId: {}", 
                     event.getBookingId(), e);
        }
    }
}
