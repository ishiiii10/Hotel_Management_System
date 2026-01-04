package com.hotelbooking.billing.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelbooking.billing.domain.Bill;
import com.hotelbooking.billing.domain.Payment;
import com.hotelbooking.billing.dto.BookingConfirmedEvent;
import com.hotelbooking.billing.dto.BookingInfoResponse;
import com.hotelbooking.billing.dto.request.MarkBillPaidRequest;
import com.hotelbooking.billing.dto.response.BillResponse;
import com.hotelbooking.billing.dto.response.PaymentResponse;
import com.hotelbooking.billing.enums.BillStatus;
import com.hotelbooking.billing.feign.BookingServiceClient;
import com.hotelbooking.billing.repository.BillRepository;
import com.hotelbooking.billing.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BillingService {

    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;
    private final BookingServiceClient bookingServiceClient;
    private final CacheManager cacheManager;

    /**
     * Generate bill when booking is confirmed
     */
    @CacheEvict(value = "bills", key = "#event.bookingId")
    public void generateBill(BookingConfirmedEvent event) {
        try {
            log.info("generateBill called for bookingId: {}", event.getBookingId());
            
            // Check if bill already exists
            if (billRepository.findByBookingId(event.getBookingId()).isPresent()) {
                log.warn("Bill already exists for bookingId: {}", event.getBookingId());
                return;
            }

            log.info("Creating new bill for bookingId: {}, amount: {}", 
                    event.getBookingId(), event.getAmount());

            Bill bill = Bill.builder()
                    .bookingId(event.getBookingId())
                    .userId(event.getUserId())
                    .hotelId(event.getHotelId())
                    .roomId(event.getRoomId())
                    .checkInDate(event.getCheckInDate())
                    .checkOutDate(event.getCheckOutDate())
                    .totalAmount(BigDecimal.valueOf(event.getAmount()))
                    .status(BillStatus.PENDING)
                    .build();

            bill = billRepository.save(bill);
            log.info("✅ Bill successfully generated! bookingId: {}, billId: {}, billNumber: {}", 
                    event.getBookingId(), bill.getId(), bill.getBillNumber());
        } catch (Exception e) {
            log.error("❌ Error generating bill for bookingId: {}", event.getBookingId(), e);
            throw new RuntimeException("Failed to generate bill for bookingId: " + event.getBookingId(), e);
        }
    }

    /**
     * Get bill by booking ID
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "bills", key = "#bookingId", unless = "#result == null")
    public BillResponse getBillByBookingId(Long bookingId) {
        // Check if bill exists
        if (billRepository.findByBookingId(bookingId).isPresent()) {
            Bill bill = billRepository.findByBookingId(bookingId).get();
            return toBillResponse(bill);
        }

        // Bill doesn't exist - check booking status
        try {
            BookingInfoResponse booking = bookingServiceClient.getBookingById(bookingId);
            if (booking == null) {
                throw new IllegalStateException("Booking not found for bookingId: " + bookingId);
            }

            String status = booking.getStatus();
            if (!"CONFIRMED".equalsIgnoreCase(status)) {
                throw new IllegalStateException(
                    "Bill not found for bookingId: " + bookingId + ". " +
                    "Booking status is: " + status + ". " +
                    "A bill is only generated when a booking is CONFIRMED. " +
                    "Please confirm the booking first using POST /api/bookings/" + bookingId + "/confirm"
                );
            }

            // Booking is confirmed but bill doesn't exist - this shouldn't happen
            // but we'll provide a helpful message
            throw new IllegalStateException(
                "Bill not found for bookingId: " + bookingId + ". " +
                "Booking is CONFIRMED but bill was not generated. " +
                "This may indicate a Kafka event processing issue. " +
                "You can manually generate the bill using POST /api/bills/generate/{bookingId}"
            );
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error checking booking status for bookingId: {}", bookingId, e);
            throw new IllegalStateException(
                "Bill not found for bookingId: " + bookingId + ". " +
                "Unable to verify booking status. " +
                "Please ensure the booking exists and is confirmed."
            );
        }
    }

    /**
     * Manually generate bill for a confirmed booking (for recovery/testing)
     */
    @CacheEvict(value = "bills", key = "#bookingId")
    public BillResponse manuallyGenerateBill(Long bookingId) {
        // Check if bill already exists
        if (billRepository.findByBookingId(bookingId).isPresent()) {
            Bill existingBill = billRepository.findByBookingId(bookingId).get();
            log.info("Bill already exists for bookingId: {}, billId: {}", bookingId, existingBill.getId());
            return toBillResponse(existingBill);
        }

        // Get booking details
        BookingInfoResponse booking = bookingServiceClient.getBookingById(bookingId);
        if (booking == null) {
            throw new IllegalStateException("Booking not found for bookingId: " + bookingId);
        }

        String status = booking.getStatus();
        if (!"CONFIRMED".equalsIgnoreCase(status)) {
            throw new IllegalStateException(
                "Cannot generate bill for bookingId: " + bookingId + ". " +
                "Booking status is: " + status + ". " +
                "Only CONFIRMED bookings can have bills. " +
                "Please confirm the booking first using POST /api/bookings/" + bookingId + "/confirm"
            );
        }

        // Generate bill
        Bill bill = Bill.builder()
                .bookingId(booking.getId())
                .userId(booking.getUserId())
                .hotelId(booking.getHotelId())
                .roomId(booking.getRoomId())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .totalAmount(booking.getTotalAmount())
                .status(BillStatus.PENDING)
                .build();

        bill = billRepository.save(bill);
        log.info("Manually generated bill for bookingId: {}, billId: {}", bookingId, bill.getId());

        return toBillResponse(bill);
    }

    /**
     * Mark bill as PAID (admin only)
     */
    public BillResponse markBillAsPaid(Long billId, String adminUsername, MarkBillPaidRequest request) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new IllegalStateException("Bill not found"));

        if (bill.getStatus() == BillStatus.PAID) {
            throw new IllegalStateException("Bill is already paid");
        }

        bill.setStatus(BillStatus.PAID);
        bill.setPaidAt(java.time.LocalDateTime.now());
        bill = billRepository.save(bill);

        // Create payment record (append-only)
        Payment payment = Payment.builder()
                .billId(bill.getId())
                .bookingId(bill.getBookingId())
                .userId(bill.getUserId())
                .amount(bill.getTotalAmount())
                .paymentMethod(request.getPaymentMethod())
                .transactionId(request.getTransactionId())
                .paymentReference(request.getPaymentReference())
                .notes(request.getNotes())
                .paidBy(adminUsername)
                .build();

        paymentRepository.save(payment);
        log.info("Bill {} marked as paid by {}", billId, adminUsername);

        // Evict user payments cache
        evictUserPaymentsCache(bill.getUserId());

        return toBillResponse(bill);
    }

    /**
     * Get all payments for current user
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "userPayments", key = "#userId")
    public List<PaymentResponse> getMyPayments(Long userId) {
        return paymentRepository.findByUserId(userId)
                .stream()
                .map(this::toPaymentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all payments (admin only)
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(this::toPaymentResponse)
                .collect(Collectors.toList());
    }

    private BillResponse toBillResponse(Bill bill) {
        return BillResponse.builder()
                .id(bill.getId())
                .bookingId(bill.getBookingId())
                .userId(bill.getUserId())
                .hotelId(bill.getHotelId())
                .roomId(bill.getRoomId())
                .checkInDate(bill.getCheckInDate())
                .checkOutDate(bill.getCheckOutDate())
                .totalAmount(bill.getTotalAmount())
                .status(bill.getStatus())
                .billNumber(bill.getBillNumber())
                .generatedAt(bill.getGeneratedAt())
                .paidAt(bill.getPaidAt())
                .createdAt(bill.getCreatedAt())
                .updatedAt(bill.getUpdatedAt())
                .build();
    }

    private PaymentResponse toPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .billId(payment.getBillId())
                .bookingId(payment.getBookingId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .paymentReference(payment.getPaymentReference())
                .notes(payment.getNotes())
                .paidBy(payment.getPaidBy())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}

