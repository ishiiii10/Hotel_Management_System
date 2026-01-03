package com.hotelbooking.notification.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hotelbooking.notification.domain.ScheduledReminder;

public interface ScheduledReminderRepository extends JpaRepository<ScheduledReminder, Long> {

    Optional<ScheduledReminder> findByBookingIdAndReminderType(Long bookingId, String reminderType);

    @Query("""
            SELECT sr FROM ScheduledReminder sr
            WHERE sr.scheduledDate = :date
            AND sr.sent = false
            AND sr.cancelled = false
            """)
    List<ScheduledReminder> findPendingRemindersForDate(@Param("date") LocalDate date);

    List<ScheduledReminder> findByBookingId(Long bookingId);
}

