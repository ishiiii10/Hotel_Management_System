package com.hotelbooking.billing.listener;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hotelbooking.billing.dto.BookingConfirmedEvent;
import com.hotelbooking.billing.dto.BookingCreatedEvent;
import com.hotelbooking.billing.exception.BillGenerationException;
import com.hotelbooking.billing.service.BillingService;

@ExtendWith(MockitoExtension.class)
class BookingEventListenerTest {

    @Mock
    private BillingService billingService;

    @InjectMocks
    private BookingEventListener bookingEventListener;

    private Map<String, Object> bookingCreatedPayload;
    private Map<String, Object> bookingConfirmedPayload;
    private ConsumerRecord<String, Object> bookingCreatedRecord;
    private ConsumerRecord<String, Object> bookingConfirmedRecord;

    @BeforeEach
    void setUp() {
        bookingCreatedPayload = new HashMap<>();
        bookingCreatedPayload.put("bookingId", 1L);
        bookingCreatedPayload.put("userId", 1L);
        bookingCreatedPayload.put("hotelId", 1L);
        bookingCreatedPayload.put("roomId", 1L);
        bookingCreatedPayload.put("checkInDate", LocalDate.now().plusDays(1).toString());
        bookingCreatedPayload.put("checkOutDate", LocalDate.now().plusDays(3).toString());
        bookingCreatedPayload.put("amount", 2000.0);
        bookingCreatedPayload.put("guestEmail", "test@example.com");
        bookingCreatedPayload.put("guestName", "Test Guest");
        bookingCreatedPayload.put("bookingSource", "PUBLIC");

        bookingConfirmedPayload = new HashMap<>();
        bookingConfirmedPayload.put("bookingId", 1L);
        bookingConfirmedPayload.put("userId", 1L);
        bookingConfirmedPayload.put("hotelId", 1L);
        bookingConfirmedPayload.put("roomId", 1L);
        bookingConfirmedPayload.put("checkInDate", LocalDate.now().plusDays(1).toString());
        bookingConfirmedPayload.put("checkOutDate", LocalDate.now().plusDays(3).toString());
        bookingConfirmedPayload.put("amount", 2000.0);
        bookingConfirmedPayload.put("guestEmail", "test@example.com");
        bookingConfirmedPayload.put("guestName", "Test Guest");

        bookingCreatedRecord = new ConsumerRecord<>("booking-created", 0, 0L, "key-1", bookingCreatedPayload);
        bookingConfirmedRecord = new ConsumerRecord<>("booking-confirmed", 0, 0L, "key-1", bookingConfirmedPayload);
    }

    @Test
    void testHandleBookingCreated_Success() {
        doNothing().when(billingService).generateBillForCreatedBooking(any(BookingCreatedEvent.class));

        bookingEventListener.handleBookingCreated(bookingCreatedRecord);

        verify(billingService).generateBillForCreatedBooking(any(BookingCreatedEvent.class));
    }

    @Test
    void testHandleBookingCreated_WalkIn() {
        bookingCreatedPayload.put("bookingSource", "WALK_IN");
        bookingCreatedRecord = new ConsumerRecord<>("booking-created", 0, 0L, "key-1", bookingCreatedPayload);
        
        doNothing().when(billingService).generateBillForCreatedBooking(any(BookingCreatedEvent.class));

        bookingEventListener.handleBookingCreated(bookingCreatedRecord);

        verify(billingService).generateBillForCreatedBooking(any(BookingCreatedEvent.class));
    }

    @Test
    void testHandleBookingCreated_BillGenerationException() {
        doThrow(new BillGenerationException("Bill generation failed"))
                .when(billingService).generateBillForCreatedBooking(any(BookingCreatedEvent.class));

        // Should not throw exception, just log error
        bookingEventListener.handleBookingCreated(bookingCreatedRecord);

        verify(billingService).generateBillForCreatedBooking(any(BookingCreatedEvent.class));
    }

    @Test
    void testHandleBookingCreated_NullRecord() {
        bookingEventListener.handleBookingCreated(null);

        verify(billingService, never()).generateBillForCreatedBooking(any(BookingCreatedEvent.class));
    }

    @Test
    void testHandleBookingCreated_NullValue() {
        ConsumerRecord<String, Object> nullValueRecord = new ConsumerRecord<>("booking-created", 0, 0L, "key-1", null);

        bookingEventListener.handleBookingCreated(nullValueRecord);

        verify(billingService, never()).generateBillForCreatedBooking(any(BookingCreatedEvent.class));
    }

    @Test
    void testHandleBookingCreated_InvalidPayloadType() {
        ConsumerRecord<String, Object> invalidRecord = new ConsumerRecord<>("booking-created", 0, 0L, "key-1", "invalid-string");

        bookingEventListener.handleBookingCreated(invalidRecord);

        verify(billingService, never()).generateBillForCreatedBooking(any(BookingCreatedEvent.class));
    }

    @Test
    void testHandleBookingCreated_MissingBookingId() {
        Map<String, Object> invalidPayload = new HashMap<>(bookingCreatedPayload);
        invalidPayload.remove("bookingId");
        ConsumerRecord<String, Object> invalidRecord = new ConsumerRecord<>("booking-created", 0, 0L, "key-1", invalidPayload);

        bookingEventListener.handleBookingCreated(invalidRecord);

        verify(billingService, never()).generateBillForCreatedBooking(any(BookingCreatedEvent.class));
    }

    @Test
    void testHandleBookingCreated_InvalidPayloadFormat() {
        Map<String, Object> invalidPayload = new HashMap<>();
        invalidPayload.put("bookingId", "invalid-number");
        ConsumerRecord<String, Object> invalidRecord = new ConsumerRecord<>("booking-created", 0, 0L, "key-1", invalidPayload);

        bookingEventListener.handleBookingCreated(invalidRecord);

        verify(billingService, never()).generateBillForCreatedBooking(any(BookingCreatedEvent.class));
    }

    @Test
    void testHandleBookingCreated_UnexpectedException() {
        doThrow(new RuntimeException("Unexpected error"))
                .when(billingService).generateBillForCreatedBooking(any(BookingCreatedEvent.class));

        // Should not throw exception, just log error
        bookingEventListener.handleBookingCreated(bookingCreatedRecord);

        verify(billingService).generateBillForCreatedBooking(any(BookingCreatedEvent.class));
    }

    @Test
    void testHandleBookingConfirmed_Success() {
        doNothing().when(billingService).generateBill(any(BookingConfirmedEvent.class));

        bookingEventListener.handleBookingConfirmed(bookingConfirmedRecord);

        verify(billingService).generateBill(any(BookingConfirmedEvent.class));
    }

    @Test
    void testHandleBookingConfirmed_BillGenerationException() {
        doThrow(new BillGenerationException("Bill generation failed"))
                .when(billingService).generateBill(any(BookingConfirmedEvent.class));

        // Should not throw exception, just log error
        bookingEventListener.handleBookingConfirmed(bookingConfirmedRecord);

        verify(billingService).generateBill(any(BookingConfirmedEvent.class));
    }

    @Test
    void testHandleBookingConfirmed_NullRecord() {
        bookingEventListener.handleBookingConfirmed(null);

        verify(billingService, never()).generateBill(any(BookingConfirmedEvent.class));
    }

    @Test
    void testHandleBookingConfirmed_NullValue() {
        ConsumerRecord<String, Object> nullValueRecord = new ConsumerRecord<>("booking-confirmed", 0, 0L, "key-1", null);

        bookingEventListener.handleBookingConfirmed(nullValueRecord);

        verify(billingService, never()).generateBill(any(BookingConfirmedEvent.class));
    }

    @Test
    void testHandleBookingConfirmed_InvalidPayloadType() {
        ConsumerRecord<String, Object> invalidRecord = new ConsumerRecord<>("booking-confirmed", 0, 0L, "key-1", "invalid-string");

        bookingEventListener.handleBookingConfirmed(invalidRecord);

        verify(billingService, never()).generateBill(any(BookingConfirmedEvent.class));
    }

    @Test
    void testHandleBookingConfirmed_MissingBookingId() {
        Map<String, Object> invalidPayload = new HashMap<>(bookingConfirmedPayload);
        invalidPayload.remove("bookingId");
        ConsumerRecord<String, Object> invalidRecord = new ConsumerRecord<>("booking-confirmed", 0, 0L, "key-1", invalidPayload);

        bookingEventListener.handleBookingConfirmed(invalidRecord);

        verify(billingService, never()).generateBill(any(BookingConfirmedEvent.class));
    }

    @Test
    void testHandleBookingConfirmed_InvalidPayloadFormat() {
        Map<String, Object> invalidPayload = new HashMap<>();
        invalidPayload.put("bookingId", "invalid-number");
        ConsumerRecord<String, Object> invalidRecord = new ConsumerRecord<>("booking-confirmed", 0, 0L, "key-1", invalidPayload);

        bookingEventListener.handleBookingConfirmed(invalidRecord);

        verify(billingService, never()).generateBill(any(BookingConfirmedEvent.class));
    }

    @Test
    void testHandleBookingConfirmed_UnexpectedException() {
        doThrow(new RuntimeException("Unexpected error"))
                .when(billingService).generateBill(any(BookingConfirmedEvent.class));

        // Should not throw exception, just log error
        bookingEventListener.handleBookingConfirmed(bookingConfirmedRecord);

        verify(billingService).generateBill(any(BookingConfirmedEvent.class));
    }

    @Test
    void testHandleBookingCreated_EmptyPayload() {
        Map<String, Object> emptyPayload = new HashMap<>();
        ConsumerRecord<String, Object> emptyRecord = new ConsumerRecord<>("booking-created", 0, 0L, "key-1", emptyPayload);

        bookingEventListener.handleBookingCreated(emptyRecord);

        verify(billingService, never()).generateBillForCreatedBooking(any(BookingCreatedEvent.class));
    }

    @Test
    void testHandleBookingConfirmed_EmptyPayload() {
        Map<String, Object> emptyPayload = new HashMap<>();
        ConsumerRecord<String, Object> emptyRecord = new ConsumerRecord<>("booking-confirmed", 0, 0L, "key-1", emptyPayload);

        bookingEventListener.handleBookingConfirmed(emptyRecord);

        verify(billingService, never()).generateBill(any(BookingConfirmedEvent.class));
    }
}

