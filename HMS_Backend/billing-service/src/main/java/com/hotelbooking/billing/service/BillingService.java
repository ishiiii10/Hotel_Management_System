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
import com.hotelbooking.billing.exception.BillAlreadyPaidException;
import com.hotelbooking.billing.exception.BillGenerationException;
import com.hotelbooking.billing.exception.BillNotFoundException;
import com.hotelbooking.billing.exception.BookingNotFoundException;
import com.hotelbooking.billing.exception.InvalidBookingStatusException;
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
     * Generate bill immediately for all bookings (both PUBLIC and WALK_IN) when created
     */
    @CacheEvict(value = "bills", key = "#event.bookingId")
    public void generateBillForCreatedBooking(com.hotelbooking.billing.dto.BookingCreatedEvent event) {
        try {
            log.info("generateBillForCreatedBooking called for bookingId: {}, source: {}", 
                    event.getBookingId(), event.getBookingSource());
            
            // Check if bill already exists
            if (billRepository.findByBookingId(event.getBookingId()).isPresent()) {
                log.warn("Bill already exists for bookingId: {}", event.getBookingId());
                return;
            }

            log.info("Creating bill for booking (source: {}). bookingId: {}, amount: {}", 
                    event.getBookingSource(), event.getBookingId(), event.getAmount());

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
            log.info("✅ Bill successfully generated for booking! bookingId: {}, billId: {}, billNumber: {}, source: {}", 
                    event.getBookingId(), bill.getId(), bill.getBillNumber(), event.getBookingSource());
        } catch (Exception e) {
            log.error("❌ Error generating bill for bookingId: {}", event.getBookingId(), e);
            throw new BillGenerationException("Failed to generate bill for bookingId: " + event.getBookingId(), e);
        }
    }

    /**
     * Generate bill when booking is confirmed (for PUBLIC bookings)
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
            throw new BillGenerationException("Failed to generate bill for bookingId: " + event.getBookingId(), e);
        }
    }

    /**
     * Get bill by bill ID
     */
    public BillResponse getBillById(Long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new BillNotFoundException("Bill not found with id: " + billId));
        return toBillResponse(bill);
    }

    /**
     * Get bill by booking ID
     * If bill doesn't exist, automatically generate it for CREATED bookings
     */
    @Transactional
    public BillResponse getBillByBookingId(Long bookingId) {
        // Check if bill exists
        if (billRepository.findByBookingId(bookingId).isPresent()) {
            Bill bill = billRepository.findByBookingId(bookingId).get();
            return toBillResponse(bill);
        }

        // Bill doesn't exist - check booking and generate bill if booking is CREATED
        try {
            BookingInfoResponse booking = bookingServiceClient.getBookingById(bookingId);
            if (booking == null) {
                throw new BookingNotFoundException("Booking not found for bookingId: " + bookingId);
            }

            String status = booking.getStatus();
            
            // If booking is CREATED, generate bill automatically
            if ("CREATED".equalsIgnoreCase(status)) {
                log.info("Bill not found for CREATED booking {}. Generating bill automatically...", bookingId);
                
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
                log.info("✅ Bill auto-generated for CREATED booking! bookingId: {}, billId: {}, billNumber: {}", 
                        bookingId, bill.getId(), bill.getBillNumber());
                
                // Evict cache after generating bill
                var billCache = cacheManager.getCache("bills");
                if (billCache != null) {
                    billCache.evict(bookingId);
                }
                
                return toBillResponse(bill);
            }

            // For other statuses, bill should already exist
            throw new BillNotFoundException(
                "Bill not found for bookingId: " + bookingId + ". " +
                "Booking status is: " + status + ". " +
                "If booking is CREATED, bill should be generated automatically. " +
                "Please try again or contact support."
            );
        } catch (BillNotFoundException | BookingNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error checking booking status for bookingId: {}", bookingId, e);
            throw new BillNotFoundException(
                "Bill not found for bookingId: " + bookingId + ". " +
                "Unable to verify booking status. " +
                "Please ensure the booking exists."
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
            throw new BookingNotFoundException("Booking not found for bookingId: " + bookingId);
        }

        String status = booking.getStatus();
        if (!"CONFIRMED".equalsIgnoreCase(status)) {
            throw new InvalidBookingStatusException(
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
     * Mark bill as PAID (admin/receptionist only)
     * For walk-in bookings, this will also confirm the booking automatically
     */
    public BillResponse markBillAsPaid(Long billId, String paidByUsername, MarkBillPaidRequest request) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new BillNotFoundException("Bill not found with id: " + billId));

        if (bill.getStatus() == BillStatus.PAID) {
            throw new BillAlreadyPaidException("Bill is already paid");
        }

        // Get booking info to check if booking needs to be confirmed
        BookingInfoResponse booking = bookingServiceClient.getBookingById(bill.getBookingId());
        boolean isCreated = booking != null && "CREATED".equalsIgnoreCase(booking.getStatus());

        bill.setStatus(BillStatus.PAID);
        bill.setPaidAt(java.time.LocalDateTime.now());
        bill = billRepository.save(bill);

        // Create payment record (append-only)
        String paidBy = (paidByUsername != null && !paidByUsername.isEmpty()) ? paidByUsername : "GUEST";
        Payment payment = Payment.builder()
                .billId(bill.getId())
                .bookingId(bill.getBookingId())
                .userId(bill.getUserId())
                .amount(bill.getTotalAmount())
                .paymentMethod(request.getPaymentMethod())
                .transactionId(request.getTransactionId())
                .paymentReference(request.getPaymentReference())
                .notes(request.getNotes())
                .paidBy(paidBy)
                .build();

        paymentRepository.save(payment);
        log.info("Bill {} marked as paid by {}", billId, paidBy);

        // Auto-confirm booking when bill is paid (for both PUBLIC and WALK_IN bookings)
        if (isCreated) {
            try {
                log.info("Auto-confirming booking {} after payment (source: {})", 
                        bill.getBookingId(), booking != null ? booking.getBookingSource() : "unknown");
                bookingServiceClient.confirmBooking(bill.getBookingId());
                log.info("✅ Booking {} confirmed successfully after payment", bill.getBookingId());
            } catch (Exception e) {
                log.error("❌ Error confirming booking {} after payment", bill.getBookingId(), e);
                // Don't throw - bill is already marked as paid, booking confirmation can be done manually
            }
        }

        // Evict user payments cache
        evictUserPaymentsCache(bill.getUserId());
        
        // Also evict bill cache by bookingId
        var billCache = cacheManager.getCache("bills");
        if (billCache != null) {
            billCache.evict(bill.getBookingId());
        }

        return toBillResponse(bill);
    }
    
    private void evictUserPaymentsCache(Long userId) {
        var cache = cacheManager.getCache("userPayments");
        if (cache != null) {
            cache.evict(userId);
        }
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

