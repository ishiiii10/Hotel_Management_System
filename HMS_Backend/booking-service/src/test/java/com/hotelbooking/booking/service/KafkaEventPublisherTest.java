package com.hotelbooking.booking.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.hotelbooking.booking.event.BookingCancelledEvent;
import com.hotelbooking.booking.event.BookingConfirmedEvent;
import com.hotelbooking.booking.event.BookingCreatedEvent;
import com.hotelbooking.booking.event.CheckoutCompletedEvent;
import com.hotelbooking.booking.event.GuestCheckedInEvent;

@ExtendWith(MockitoExtension.class)
class KafkaEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private KafkaEventPublisher kafkaEventPublisher;

    private BookingCreatedEvent bookingCreatedEvent;
    private BookingConfirmedEvent bookingConfirmedEvent;
    private BookingCancelledEvent bookingCancelledEvent;
    private GuestCheckedInEvent guestCheckedInEvent;
    private CheckoutCompletedEvent checkoutCompletedEvent;

    @BeforeEach
    void setUp() {
        bookingCreatedEvent = BookingCreatedEvent.builder()
                .bookingId(1L)
                .userId(1L)
                .hotelId(1L)
                .roomId(1L)
                .checkInDate(LocalDate.now().plusDays(1))
                .checkOutDate(LocalDate.now().plusDays(3))
                .amount(2000.0)
                .guestEmail("test@example.com")
                .guestName("Test Guest")
                .bookingSource("PUBLIC")
                .build();

        bookingConfirmedEvent = BookingConfirmedEvent.builder()
                .bookingId(1L)
                .userId(1L)
                .hotelId(1L)
                .roomId(1L)
                .checkInDate(LocalDate.now().plusDays(1))
                .checkOutDate(LocalDate.now().plusDays(3))
                .amount(2000.0)
                .guestEmail("test@example.com")
                .guestName("Test Guest")
                .build();

        bookingCancelledEvent = BookingCancelledEvent.builder()
                .bookingId(1L)
                .userId(1L)
                .hotelId(1L)
                .checkInDate(LocalDate.now().plusDays(1))
                .checkOutDate(LocalDate.now().plusDays(3))
                .cancellationReason("Change of plans")
                .guestEmail("test@example.com")
                .guestName("Test Guest")
                .build();

        guestCheckedInEvent = GuestCheckedInEvent.builder()
                .bookingId(1L)
                .userId(1L)
                .hotelId(1L)
                .checkInDate(LocalDate.now().plusDays(1))
                .actualCheckInTimestamp(LocalDateTime.now())
                .guestEmail("test@example.com")
                .guestName("Test Guest")
                .build();

        checkoutCompletedEvent = CheckoutCompletedEvent.builder()
                .bookingId(1L)
                .userId(1L)
                .hotelId(1L)
                .checkInDate(LocalDate.now().plusDays(1))
                .checkOutDate(LocalDate.now().plusDays(3))
                .actualCheckoutTimestamp(LocalDateTime.now())
                .guestEmail("test@example.com")
                .guestName("Test Guest")
                .build();
    }

    @Test
    void testPublishBookingCreated_Success() {
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(null);

        kafkaEventPublisher.publishBookingCreated(bookingCreatedEvent);

        verify(kafkaTemplate).send(eq("booking-created"), eq("1"), eq(bookingCreatedEvent));
    }

    @Test
    void testPublishBookingCreated_Exception() {
        doThrow(new RuntimeException("Kafka error")).when(kafkaTemplate).send(anyString(), anyString(), any());

        // Should not throw exception, just log error
        kafkaEventPublisher.publishBookingCreated(bookingCreatedEvent);

        verify(kafkaTemplate).send(eq("booking-created"), eq("1"), eq(bookingCreatedEvent));
    }

    @Test
    void testPublishBookingConfirmed_Success() {
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(null);

        kafkaEventPublisher.publishBookingConfirmed(bookingConfirmedEvent);

        verify(kafkaTemplate).send(eq("booking-confirmed"), eq("1"), eq(bookingConfirmedEvent));
    }

    @Test
    void testPublishBookingConfirmed_Exception() {
        doThrow(new RuntimeException("Kafka error")).when(kafkaTemplate).send(anyString(), anyString(), any());

        kafkaEventPublisher.publishBookingConfirmed(bookingConfirmedEvent);

        verify(kafkaTemplate).send(eq("booking-confirmed"), eq("1"), eq(bookingConfirmedEvent));
    }

    @Test
    void testPublishBookingCancelled_Success() {
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(null);

        kafkaEventPublisher.publishBookingCancelled(bookingCancelledEvent);

        verify(kafkaTemplate).send(eq("booking-cancelled"), eq("1"), eq(bookingCancelledEvent));
    }

    @Test
    void testPublishBookingCancelled_Exception() {
        doThrow(new RuntimeException("Kafka error")).when(kafkaTemplate).send(anyString(), anyString(), any());

        kafkaEventPublisher.publishBookingCancelled(bookingCancelledEvent);

        verify(kafkaTemplate).send(eq("booking-cancelled"), eq("1"), eq(bookingCancelledEvent));
    }

    @Test
    void testPublishGuestCheckedIn_Success() {
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(null);

        kafkaEventPublisher.publishGuestCheckedIn(guestCheckedInEvent);

        verify(kafkaTemplate).send(eq("guest-checked-in"), eq("1"), eq(guestCheckedInEvent));
    }

    @Test
    void testPublishGuestCheckedIn_Exception() {
        doThrow(new RuntimeException("Kafka error")).when(kafkaTemplate).send(anyString(), anyString(), any());

        kafkaEventPublisher.publishGuestCheckedIn(guestCheckedInEvent);

        verify(kafkaTemplate).send(eq("guest-checked-in"), eq("1"), eq(guestCheckedInEvent));
    }

    @Test
    void testPublishCheckoutCompleted_Success() {
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(null);

        kafkaEventPublisher.publishCheckoutCompleted(checkoutCompletedEvent);

        verify(kafkaTemplate).send(eq("checkout-completed"), eq("1"), eq(checkoutCompletedEvent));
    }

    @Test
    void testPublishCheckoutCompleted_Exception() {
        doThrow(new RuntimeException("Kafka error")).when(kafkaTemplate).send(anyString(), anyString(), any());

        kafkaEventPublisher.publishCheckoutCompleted(checkoutCompletedEvent);

        verify(kafkaTemplate).send(eq("checkout-completed"), eq("1"), eq(checkoutCompletedEvent));
    }
}

