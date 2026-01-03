package com.hotelbooking.billing.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotelbooking.billing.dto.request.MarkBillPaidRequest;
import com.hotelbooking.billing.dto.response.BillResponse;
import com.hotelbooking.billing.dto.response.PaymentResponse;
import com.hotelbooking.billing.service.BillingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/bills")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    /**
     * Get bill by booking ID
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<?> getBillByBookingId(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long bookingId
    ) {
        BillResponse bill = billingService.getBillByBookingId(bookingId);

        // Authorization: Users can only view their own bills unless ADMIN
        if (!"ADMIN".equalsIgnoreCase(role) && !bill.getUserId().equals(userId)) {
            throw new IllegalStateException("You can only view your own bills");
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Bill retrieved successfully",
                "data", bill
        ));
    }

    /**
     * Mark bill as PAID (admin only)
     */
    @PostMapping("/{billId}/mark-paid")
    public ResponseEntity<?> markBillAsPaid(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Username") String username,
            @PathVariable Long billId,
            @Valid @RequestBody MarkBillPaidRequest request
    ) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new IllegalStateException("Only ADMIN can mark bills as paid");
        }

        BillResponse bill = billingService.markBillAsPaid(billId, username, request);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Bill marked as paid successfully",
                "data", bill
        ));
    }

    /**
     * Get all payments for current user
     */
    @GetMapping("/my-payments")
    public ResponseEntity<?> getMyPayments(@RequestHeader("X-User-Id") Long userId) {
        List<PaymentResponse> payments = billingService.getMyPayments(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Payments retrieved successfully",
                "data", payments
        ));
    }

    /**
     * Get all payments (admin only)
     */
    @GetMapping("/payments")
    public ResponseEntity<?> getAllPayments(@RequestHeader("X-User-Role") String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new IllegalStateException("Only ADMIN can view all payments");
        }

        List<PaymentResponse> payments = billingService.getAllPayments();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "All payments retrieved successfully",
                "data", payments
        ));
    }
}

