package com.hotelbooking.notification.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.hotelbooking.notification.domain.ScheduledReminder;
import com.hotelbooking.notification.dto.HotelInfoResponse;
import com.hotelbooking.notification.feign.HotelServiceClient;
import com.hotelbooking.notification.repository.ScheduledReminderRepository;
import com.hotelbooking.notification.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {

    private final ScheduledReminderRepository reminderRepository;
    private final EmailService emailService;
    private final HotelServiceClient hotelServiceClient;

    /**
     * Runs every hour to check for reminders that need to be sent
     */
    @Scheduled(cron = "0 0 * * * ?") // Every hour at minute 0
    @Transactional
    public void processScheduledReminders() {
        try {
            LocalDate today = LocalDate.now();
            log.info("Processing scheduled reminders for date: {}", today);

            List<ScheduledReminder> reminders = reminderRepository.findPendingRemindersForDate(today);

            for (ScheduledReminder reminder : reminders) {
                try {
                    sendReminder(reminder);
                    reminder.setSent(true);
                    reminder.setSentAt(LocalDateTime.now());
                    reminderRepository.save(reminder);
                    log.info("Reminder sent successfully for bookingId: {}", reminder.getBookingId());
                } catch (Exception e) {
                    log.error("Failed to send reminder for bookingId: {}", 
                             reminder.getBookingId(), e);
                    // Don't mark as sent if it failed - will retry next hour
                }
            }

            log.info("Processed {} reminders", reminders.size());
        } catch (Exception e) {
            log.error("Error processing scheduled reminders", e);
        }
    }

    private void sendReminder(ScheduledReminder reminder) {
        if ("CHECK_IN_REMINDER".equals(reminder.getReminderType())) {
            // Fetch hotel details
            HotelInfoResponse hotel = hotelServiceClient.getHotelById(reminder.getHotelId());
            if (hotel == null) {
                log.warn("Hotel not found for hotelId: {}", reminder.getHotelId());
                return;
            }

            String guestName = reminder.getGuestName() != null ? reminder.getGuestName() : "Guest";
            String hotelAddress = hotel.getAddress() != null ? hotel.getAddress() : "";

            emailService.sendCheckInReminderEmail(
                reminder.getGuestEmail(),
                guestName,
                hotel.getName(),
                reminder.getCheckInDate().toString(),
                hotelAddress
            );
        }
    }
}

