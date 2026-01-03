package com.hotelbooking.billing.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelbooking.billing.domain.Bill;
import com.hotelbooking.billing.domain.Payment;
import com.hotelbooking.billing.dto.BookingConfirmedEvent;
import com.hotelbooking.billing.dto.request.MarkBillPaidRequest;
import com.hotelbooking.billing.dto.response.BillResponse;
import com.hotelbooking.billing.dto.response.PaymentResponse;
import com.hotelbooking.billing.enums.BillStatus;
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

    /**
     * Generate bill when booking is confirmed
     */
    public void generateBill(BookingConfirmedEvent event) {
        try {
            // Check if bill already exists
            if (billRepository.findByBookingId(event.getBookingId()).isPresent()) {
                log.warn("Bill already exists for bookingId: {}", event.getBookingId());
                return;
            }

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
            log.info("Bill generated for bookingId: {}, billId: {}", event.getBookingId(), bill.getId());
        } catch (Exception e) {
            log.error("Error generating bill for bookingId: {}", event.getBookingId(), e);
        }
    }

    /**
     * Get bill by booking ID
     */
    @Transactional(readOnly = true)
    public BillResponse getBillByBookingId(Long bookingId) {
        Bill bill = billRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new IllegalStateException("Bill not found for bookingId: " + bookingId));
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

        return toBillResponse(bill);
    }

    /**
     * Get all payments for current user
     */
    @Transactional(readOnly = true)
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

