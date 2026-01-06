package com.hotelbooking.billing.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import com.hotelbooking.billing.domain.Bill;
import com.hotelbooking.billing.domain.Payment;
import com.hotelbooking.billing.dto.BookingConfirmedEvent;
import com.hotelbooking.billing.dto.BookingCreatedEvent;
import com.hotelbooking.billing.dto.BookingInfoResponse;
import com.hotelbooking.billing.dto.request.MarkBillPaidRequest;
import com.hotelbooking.billing.dto.response.BillResponse;
import com.hotelbooking.billing.dto.response.PaymentResponse;
import com.hotelbooking.billing.enums.BillStatus;
import com.hotelbooking.billing.exception.BillAlreadyPaidException;
import com.hotelbooking.billing.exception.BillGenerationException;
import com.hotelbooking.billing.exception.BillNotFoundException;
import com.hotelbooking.billing.exception.BookingNotFoundException;
import com.hotelbooking.billing.exception.InvalidBookingStatusException;
import com.hotelbooking.billing.feign.BookingServiceClient;
import com.hotelbooking.billing.repository.BillRepository;
import com.hotelbooking.billing.repository.PaymentRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

    @Mock
    private BillRepository billRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingServiceClient bookingServiceClient;

    @Mock
    private CacheManager cacheManager;

    @InjectMocks
    private BillingService billingService;

    private Bill testBill;
    private Payment testPayment;
    private BookingCreatedEvent bookingCreatedEvent;
    private BookingConfirmedEvent bookingConfirmedEvent;
    private BookingInfoResponse bookingInfo;

    @BeforeEach
    void setUp() {
        testBill = Bill.builder()
                .id(1L)
                .bookingId(1L)
                .userId(1L)
                .hotelId(1L)
                .roomId(1L)
                .checkInDate(LocalDate.now().plusDays(1))
                .checkOutDate(LocalDate.now().plusDays(3))
                .totalAmount(BigDecimal.valueOf(2000))
                .status(BillStatus.PENDING)
                .billNumber("BILL-123")
                .generatedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testPayment = Payment.builder()
                .id(1L)
                .billId(1L)
                .bookingId(1L)
                .userId(1L)
                .amount(BigDecimal.valueOf(2000))
                .paymentMethod("CASH")
                .paidBy("admin")
                .paidAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        bookingCreatedEvent = BookingCreatedEvent.builder()
                .bookingId(1L)
                .userId(1L)
                .hotelId(1L)
                .roomId(1L)
                .checkInDate(LocalDate.now().plusDays(1))
                .checkOutDate(LocalDate.now().plusDays(3))
                .amount(2000.0)
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
                .build();

        bookingInfo = BookingInfoResponse.builder()
                .id(1L)
                .userId(1L)
                .hotelId(1L)
                .roomId(1L)
                .checkInDate(LocalDate.now().plusDays(1))
                .checkOutDate(LocalDate.now().plusDays(3))
                .totalAmount(BigDecimal.valueOf(2000))
                .status("CREATED")
                .bookingSource("PUBLIC")
                .build();

        // Setup cache manager
        Cache billCache = new ConcurrentMapCacheManager().getCache("bills");
        Cache userPaymentsCache = new ConcurrentMapCacheManager().getCache("userPayments");
        lenient().when(cacheManager.getCache("bills")).thenReturn(billCache);
        lenient().when(cacheManager.getCache("userPayments")).thenReturn(userPaymentsCache);
    }

    @Test
    void testGenerateBillForCreatedBooking_Success() {
        when(billRepository.findByBookingId(1L)).thenReturn(Optional.empty());
        when(billRepository.save(any(Bill.class))).thenReturn(testBill);

        billingService.generateBillForCreatedBooking(bookingCreatedEvent);

        verify(billRepository).findByBookingId(1L);
        verify(billRepository).save(any(Bill.class));
    }

    @Test
    void testGenerateBillForCreatedBooking_BillAlreadyExists() {
        when(billRepository.findByBookingId(1L)).thenReturn(Optional.of(testBill));

        billingService.generateBillForCreatedBooking(bookingCreatedEvent);

        verify(billRepository).findByBookingId(1L);
        verify(billRepository, never()).save(any(Bill.class));
    }

    @Test
    void testGenerateBillForCreatedBooking_Exception() {
        when(billRepository.findByBookingId(1L)).thenReturn(Optional.empty());
        when(billRepository.save(any(Bill.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(BillGenerationException.class, () -> {
            billingService.generateBillForCreatedBooking(bookingCreatedEvent);
        });
    }

    @Test
    void testGenerateBill_Success() {
        when(billRepository.findByBookingId(1L)).thenReturn(Optional.empty());
        when(billRepository.save(any(Bill.class))).thenReturn(testBill);

        billingService.generateBill(bookingConfirmedEvent);

        verify(billRepository).findByBookingId(1L);
        verify(billRepository).save(any(Bill.class));
    }

    @Test
    void testGenerateBill_BillAlreadyExists() {
        when(billRepository.findByBookingId(1L)).thenReturn(Optional.of(testBill));

        billingService.generateBill(bookingConfirmedEvent);

        verify(billRepository).findByBookingId(1L);
        verify(billRepository, never()).save(any(Bill.class));
    }

    @Test
    void testGenerateBill_Exception() {
        when(billRepository.findByBookingId(1L)).thenReturn(Optional.empty());
        when(billRepository.save(any(Bill.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(BillGenerationException.class, () -> {
            billingService.generateBill(bookingConfirmedEvent);
        });
    }

    @Test
    void testGetBillById_Success() {
        when(billRepository.findById(1L)).thenReturn(Optional.of(testBill));

        BillResponse response = billingService.getBillById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getBookingId());
        verify(billRepository).findById(1L);
    }

    @Test
    void testGetBillById_NotFound() {
        when(billRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BillNotFoundException.class, () -> {
            billingService.getBillById(1L);
        });
    }

    @Test
    void testGetBillByBookingId_BillExists() {
        when(billRepository.findByBookingId(1L)).thenReturn(Optional.of(testBill));

        BillResponse response = billingService.getBillByBookingId(1L);

        assertNotNull(response);
        assertEquals(1L, response.getBookingId());
        verify(billRepository, times(2)).findByBookingId(1L);
        verify(bookingServiceClient, never()).getBookingById(anyLong());
    }

    @Test
    void testGetBillByBookingId_BillNotExists_CreatedBooking() {
        when(billRepository.findByBookingId(1L)).thenReturn(Optional.empty());
        when(bookingServiceClient.getBookingById(1L)).thenReturn(bookingInfo);
        when(billRepository.save(any(Bill.class))).thenReturn(testBill);
        when(cacheManager.getCache("bills")).thenReturn(new ConcurrentMapCacheManager().getCache("bills"));

        BillResponse response = billingService.getBillByBookingId(1L);

        assertNotNull(response);
        assertEquals(1L, response.getBookingId());
        verify(billRepository).save(any(Bill.class));
    }

    @Test
    void testGetBillByBookingId_BillNotExists_BookingNotFound() {
        when(billRepository.findByBookingId(1L)).thenReturn(Optional.empty());
        when(bookingServiceClient.getBookingById(1L)).thenReturn(null);

        assertThrows(BookingNotFoundException.class, () -> {
            billingService.getBillByBookingId(1L);
        });
    }

    @Test
    void testGetBillByBookingId_BillNotExists_NonCreatedStatus() {
        bookingInfo.setStatus("CONFIRMED");
        when(billRepository.findByBookingId(1L)).thenReturn(Optional.empty());
        when(bookingServiceClient.getBookingById(1L)).thenReturn(bookingInfo);

        assertThrows(BillNotFoundException.class, () -> {
            billingService.getBillByBookingId(1L);
        });
    }

    @Test
    void testGetBillByBookingId_Exception() {
        when(billRepository.findByBookingId(1L)).thenReturn(Optional.empty());
        when(bookingServiceClient.getBookingById(1L)).thenThrow(new RuntimeException("Service error"));

        assertThrows(BillNotFoundException.class, () -> {
            billingService.getBillByBookingId(1L);
        });
    }

    @Test
    void testManuallyGenerateBill_BillExists() {
        when(billRepository.findByBookingId(1L)).thenReturn(Optional.of(testBill));

        BillResponse response = billingService.manuallyGenerateBill(1L);

        assertNotNull(response);
        assertEquals(1L, response.getBookingId());
        verify(billRepository, never()).save(any(Bill.class));
    }

    @Test
    void testManuallyGenerateBill_BookingNotFound() {
        when(billRepository.findByBookingId(1L)).thenReturn(Optional.empty());
        when(bookingServiceClient.getBookingById(1L)).thenReturn(null);

        assertThrows(BookingNotFoundException.class, () -> {
            billingService.manuallyGenerateBill(1L);
        });
    }

    @Test
    void testManuallyGenerateBill_InvalidStatus() {
        bookingInfo.setStatus("CANCELLED");
        when(billRepository.findByBookingId(1L)).thenReturn(Optional.empty());
        when(bookingServiceClient.getBookingById(1L)).thenReturn(bookingInfo);

        assertThrows(InvalidBookingStatusException.class, () -> {
            billingService.manuallyGenerateBill(1L);
        });
    }

    @Test
    void testManuallyGenerateBill_Success() {
        bookingInfo.setStatus("CONFIRMED");
        when(billRepository.findByBookingId(1L)).thenReturn(Optional.empty());
        when(bookingServiceClient.getBookingById(1L)).thenReturn(bookingInfo);
        when(billRepository.save(any(Bill.class))).thenReturn(testBill);

        BillResponse response = billingService.manuallyGenerateBill(1L);

        assertNotNull(response);
        assertEquals(1L, response.getBookingId());
        verify(billRepository).save(any(Bill.class));
    }

    @Test
    void testMarkBillAsPaid_Success() {
        bookingInfo.setStatus("CREATED");
        MarkBillPaidRequest request = new MarkBillPaidRequest();
        request.setPaymentMethod("CASH");
        request.setTransactionId("TXN-123");

        when(billRepository.findById(1L)).thenReturn(Optional.of(testBill));
        when(bookingServiceClient.getBookingById(1L)).thenReturn(bookingInfo);
        when(billRepository.save(any(Bill.class))).thenReturn(testBill);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(bookingServiceClient.confirmBooking(1L)).thenReturn(bookingInfo);
        when(cacheManager.getCache("bills")).thenReturn(new ConcurrentMapCacheManager().getCache("bills"));
        when(cacheManager.getCache("userPayments")).thenReturn(new ConcurrentMapCacheManager().getCache("userPayments"));

        BillResponse response = billingService.markBillAsPaid(1L, "admin", request);

        assertNotNull(response);
        assertEquals(BillStatus.PAID, response.getStatus());
        verify(billRepository).save(any(Bill.class));
        verify(paymentRepository).save(any(Payment.class));
        verify(bookingServiceClient).confirmBooking(1L);
    }

    @Test
    void testMarkBillAsPaid_BillNotFound() {
        MarkBillPaidRequest request = new MarkBillPaidRequest();
        request.setPaymentMethod("CASH");

        when(billRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BillNotFoundException.class, () -> {
            billingService.markBillAsPaid(1L, "admin", request);
        });
    }

    @Test
    void testMarkBillAsPaid_AlreadyPaid() {
        testBill.setStatus(BillStatus.PAID);
        MarkBillPaidRequest request = new MarkBillPaidRequest();
        request.setPaymentMethod("CASH");

        when(billRepository.findById(1L)).thenReturn(Optional.of(testBill));

        assertThrows(BillAlreadyPaidException.class, () -> {
            billingService.markBillAsPaid(1L, "admin", request);
        });
    }

    @Test
    void testMarkBillAsPaid_NonCreatedBooking() {
        bookingInfo.setStatus("CONFIRMED");
        MarkBillPaidRequest request = new MarkBillPaidRequest();
        request.setPaymentMethod("CASH");

        when(billRepository.findById(1L)).thenReturn(Optional.of(testBill));
        when(bookingServiceClient.getBookingById(1L)).thenReturn(bookingInfo);
        when(billRepository.save(any(Bill.class))).thenReturn(testBill);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(cacheManager.getCache("bills")).thenReturn(new ConcurrentMapCacheManager().getCache("bills"));
        when(cacheManager.getCache("userPayments")).thenReturn(new ConcurrentMapCacheManager().getCache("userPayments"));

        BillResponse response = billingService.markBillAsPaid(1L, "admin", request);

        assertNotNull(response);
        verify(bookingServiceClient, never()).confirmBooking(anyLong());
    }

    @Test
    void testMarkBillAsPaid_ConfirmBookingException() {
        bookingInfo.setStatus("CREATED");
        MarkBillPaidRequest request = new MarkBillPaidRequest();
        request.setPaymentMethod("CASH");

        when(billRepository.findById(1L)).thenReturn(Optional.of(testBill));
        when(bookingServiceClient.getBookingById(1L)).thenReturn(bookingInfo);
        when(billRepository.save(any(Bill.class))).thenReturn(testBill);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(bookingServiceClient.confirmBooking(1L)).thenThrow(new RuntimeException("Service error"));
        when(cacheManager.getCache("bills")).thenReturn(new ConcurrentMapCacheManager().getCache("bills"));
        when(cacheManager.getCache("userPayments")).thenReturn(new ConcurrentMapCacheManager().getCache("userPayments"));

        // Should not throw exception even if booking confirmation fails
        BillResponse response = billingService.markBillAsPaid(1L, "admin", request);

        assertNotNull(response);
        assertEquals(BillStatus.PAID, response.getStatus());
    }

    @Test
    void testMarkBillAsPaid_NullPaidBy() {
        bookingInfo.setStatus("CONFIRMED");
        MarkBillPaidRequest request = new MarkBillPaidRequest();
        request.setPaymentMethod("CASH");

        when(billRepository.findById(1L)).thenReturn(Optional.of(testBill));
        when(bookingServiceClient.getBookingById(1L)).thenReturn(bookingInfo);
        when(billRepository.save(any(Bill.class))).thenReturn(testBill);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(cacheManager.getCache("bills")).thenReturn(new ConcurrentMapCacheManager().getCache("bills"));
        when(cacheManager.getCache("userPayments")).thenReturn(new ConcurrentMapCacheManager().getCache("userPayments"));

        BillResponse response = billingService.markBillAsPaid(1L, null, request);

        assertNotNull(response);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void testGetMyPayments_Success() {
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentRepository.findByUserId(1L)).thenReturn(payments);

        List<PaymentResponse> response = billingService.getMyPayments(1L);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(1L, response.get(0).getId());
        verify(paymentRepository).findByUserId(1L);
    }

    @Test
    void testGetMyPayments_Empty() {
        when(paymentRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        List<PaymentResponse> response = billingService.getMyPayments(1L);

        assertNotNull(response);
        assertEquals(0, response.size());
    }

    @Test
    void testGetAllPayments_Success() {
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentRepository.findAll()).thenReturn(payments);

        List<PaymentResponse> response = billingService.getAllPayments();

        assertNotNull(response);
        assertEquals(1, response.size());
        verify(paymentRepository).findAll();
    }

    @Test
    void testGetAllPayments_Empty() {
        when(paymentRepository.findAll()).thenReturn(Collections.emptyList());

        List<PaymentResponse> response = billingService.getAllPayments();

        assertNotNull(response);
        assertEquals(0, response.size());
    }

    @Test
    void testToBillResponse() {
        when(billRepository.findById(1L)).thenReturn(Optional.of(testBill));

        BillResponse response = billingService.getBillById(1L);

        assertNotNull(response);
        assertEquals(testBill.getId(), response.getId());
        assertEquals(testBill.getBookingId(), response.getBookingId());
        assertEquals(testBill.getUserId(), response.getUserId());
        assertEquals(testBill.getHotelId(), response.getHotelId());
        assertEquals(testBill.getRoomId(), response.getRoomId());
        assertEquals(testBill.getTotalAmount(), response.getTotalAmount());
        assertEquals(testBill.getStatus(), response.getStatus());
        assertEquals(testBill.getBillNumber(), response.getBillNumber());
    }

    @Test
    void testToPaymentResponse() {
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentRepository.findByUserId(1L)).thenReturn(payments);

        List<PaymentResponse> response = billingService.getMyPayments(1L);

        assertNotNull(response);
        assertEquals(1, response.size());
        PaymentResponse paymentResponse = response.get(0);
        assertEquals(testPayment.getId(), paymentResponse.getId());
        assertEquals(testPayment.getBillId(), paymentResponse.getBillId());
        assertEquals(testPayment.getBookingId(), paymentResponse.getBookingId());
        assertEquals(testPayment.getAmount(), paymentResponse.getAmount());
        assertEquals(testPayment.getPaymentMethod(), paymentResponse.getPaymentMethod());
    }
}

