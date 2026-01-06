package com.hotelbooking.billing.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.hotelbooking.billing.dto.request.MarkBillPaidRequest;
import com.hotelbooking.billing.dto.response.BillResponse;
import com.hotelbooking.billing.dto.response.PaymentResponse;
import com.hotelbooking.billing.enums.BillStatus;
import com.hotelbooking.billing.exception.AccessDeniedException;
import com.hotelbooking.billing.exception.BillAlreadyPaidException;
import com.hotelbooking.billing.exception.BillNotFoundException;
import com.hotelbooking.billing.service.BillingService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillingControllerTest {

    @Mock
    private BillingService billingService;

    @InjectMocks
    private BillingController billingController;

    private BillResponse testBill;
    private PaymentResponse testPayment;
    private MarkBillPaidRequest markBillPaidRequest;

    @BeforeEach
    void setUp() {
        testBill = BillResponse.builder()
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

        testPayment = PaymentResponse.builder()
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

        markBillPaidRequest = new MarkBillPaidRequest();
        markBillPaidRequest.setPaymentMethod("CASH");
        markBillPaidRequest.setTransactionId("TXN-123");
    }

    @Test
    void testGetBillByBookingId_Admin() {
        when(billingService.getBillByBookingId(1L)).thenReturn(testBill);

        ResponseEntity<?> response = billingController.getBillByBookingId(1L, "ADMIN", null, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(true, body.get("success"));
        assertEquals("Bill retrieved successfully", body.get("message"));
        verify(billingService).getBillByBookingId(1L);
    }

    @Test
    void testGetBillByBookingId_Manager_Success() {
        when(billingService.getBillByBookingId(1L)).thenReturn(testBill);

        ResponseEntity<?> response = billingController.getBillByBookingId(1L, "MANAGER", 1L, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(billingService).getBillByBookingId(1L);
    }

    @Test
    void testGetBillByBookingId_Manager_WrongHotel() {
        when(billingService.getBillByBookingId(1L)).thenReturn(testBill);

        assertThrows(AccessDeniedException.class, () -> {
            billingController.getBillByBookingId(1L, "MANAGER", 2L, 1L);
        });
    }

    @Test
    void testGetBillByBookingId_Manager_NullHotelId() {
        when(billingService.getBillByBookingId(1L)).thenReturn(testBill);

        assertThrows(AccessDeniedException.class, () -> {
            billingController.getBillByBookingId(1L, "MANAGER", null, 1L);
        });
    }

    @Test
    void testGetBillByBookingId_Receptionist_Success() {
        when(billingService.getBillByBookingId(1L)).thenReturn(testBill);

        ResponseEntity<?> response = billingController.getBillByBookingId(1L, "RECEPTIONIST", 1L, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(billingService).getBillByBookingId(1L);
    }

    @Test
    void testGetBillByBookingId_Guest_Success() {
        when(billingService.getBillByBookingId(1L)).thenReturn(testBill);

        ResponseEntity<?> response = billingController.getBillByBookingId(1L, "GUEST", null, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(billingService).getBillByBookingId(1L);
    }

    @Test
    void testGetBillByBookingId_Guest_WrongUser() {
        when(billingService.getBillByBookingId(1L)).thenReturn(testBill);

        assertThrows(AccessDeniedException.class, () -> {
            billingController.getBillByBookingId(2L, "GUEST", null, 1L);
        });
    }

    @Test
    void testGetBillByBookingId_UnauthorizedRole() {
        when(billingService.getBillByBookingId(1L)).thenReturn(testBill);

        assertThrows(AccessDeniedException.class, () -> {
            billingController.getBillByBookingId(1L, "UNKNOWN", null, 1L);
        });
    }

    @Test
    void testGetBillByBookingId_BillNotFound() {
        when(billingService.getBillByBookingId(1L)).thenThrow(new BillNotFoundException("Bill not found"));

        assertThrows(BillNotFoundException.class, () -> {
            billingController.getBillByBookingId(1L, "ADMIN", null, 1L);
        });
    }

    @Test
    void testManuallyGenerateBill_Admin() {
        when(billingService.manuallyGenerateBill(1L)).thenReturn(testBill);

        ResponseEntity<?> response = billingController.manuallyGenerateBill("ADMIN", 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(true, body.get("success"));
        assertEquals("Bill generated successfully", body.get("message"));
        verify(billingService).manuallyGenerateBill(1L);
    }

    @Test
    void testManuallyGenerateBill_NonAdmin() {
        assertThrows(AccessDeniedException.class, () -> {
            billingController.manuallyGenerateBill("MANAGER", 1L);
        });
    }

    @Test
    void testManuallyGenerateBill_Guest() {
        assertThrows(AccessDeniedException.class, () -> {
            billingController.manuallyGenerateBill("GUEST", 1L);
        });
    }

    @Test
    void testMarkBillAsPaid_Admin() {
        when(billingService.markBillAsPaid(1L, "admin", markBillPaidRequest)).thenReturn(testBill);

        ResponseEntity<?> response = billingController.markBillAsPaid(1L, "ADMIN", "admin", 1L, markBillPaidRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(true, body.get("success"));
        assertEquals("Bill marked as paid successfully", body.get("message"));
        verify(billingService).markBillAsPaid(1L, "admin", markBillPaidRequest);
    }

    @Test
    void testMarkBillAsPaid_Manager() {
        when(billingService.markBillAsPaid(1L, "manager", markBillPaidRequest)).thenReturn(testBill);

        ResponseEntity<?> response = billingController.markBillAsPaid(1L, "MANAGER", "manager", 1L, markBillPaidRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(billingService).markBillAsPaid(1L, "manager", markBillPaidRequest);
    }

    @Test
    void testMarkBillAsPaid_Receptionist() {
        when(billingService.markBillAsPaid(1L, "receptionist", markBillPaidRequest)).thenReturn(testBill);

        ResponseEntity<?> response = billingController.markBillAsPaid(1L, "RECEPTIONIST", "receptionist", 1L, markBillPaidRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(billingService).markBillAsPaid(1L, "receptionist", markBillPaidRequest);
    }

    @Test
    void testMarkBillAsPaid_Guest_Success() {
        when(billingService.getBillById(1L)).thenReturn(testBill);
        when(billingService.markBillAsPaid(1L, "guest", markBillPaidRequest)).thenReturn(testBill);

        ResponseEntity<?> response = billingController.markBillAsPaid(1L, "GUEST", "guest", 1L, markBillPaidRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(billingService).getBillById(1L);
        verify(billingService).markBillAsPaid(1L, "guest", markBillPaidRequest);
    }

    @Test
    void testMarkBillAsPaid_Guest_WrongUser() {
        when(billingService.getBillById(1L)).thenReturn(testBill);

        assertThrows(AccessDeniedException.class, () -> {
            billingController.markBillAsPaid(2L, "GUEST", "guest", 1L, markBillPaidRequest);
        });
    }

    @Test
    void testMarkBillAsPaid_UnauthorizedRole() {
        assertThrows(AccessDeniedException.class, () -> {
            billingController.markBillAsPaid(1L, "UNKNOWN", "user", 1L, markBillPaidRequest);
        });
    }

    @Test
    void testMarkBillAsPaid_BillNotFound() {
        when(billingService.getBillById(1L)).thenThrow(new BillNotFoundException("Bill not found"));

        assertThrows(BillNotFoundException.class, () -> {
            billingController.markBillAsPaid(1L, "GUEST", "guest", 1L, markBillPaidRequest);
        });
    }

    @Test
    void testMarkBillAsPaid_AlreadyPaid() {
        when(billingService.getBillById(1L)).thenReturn(testBill);
        when(billingService.markBillAsPaid(1L, "guest", markBillPaidRequest))
                .thenThrow(new BillAlreadyPaidException("Bill is already paid"));

        assertThrows(BillAlreadyPaidException.class, () -> {
            billingController.markBillAsPaid(1L, "GUEST", "guest", 1L, markBillPaidRequest);
        });
    }

    @Test
    void testGetMyPayments_Success() {
        List<PaymentResponse> payments = Arrays.asList(testPayment);
        when(billingService.getMyPayments(1L)).thenReturn(payments);

        ResponseEntity<?> response = billingController.getMyPayments(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(true, body.get("success"));
        assertEquals("Payments retrieved successfully", body.get("message"));
        verify(billingService).getMyPayments(1L);
    }

    @Test
    void testGetMyPayments_Empty() {
        when(billingService.getMyPayments(1L)).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = billingController.getMyPayments(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(true, body.get("success"));
    }

    @Test
    void testGetAllPayments_Admin() {
        List<PaymentResponse> payments = Arrays.asList(testPayment);
        when(billingService.getAllPayments()).thenReturn(payments);

        ResponseEntity<?> response = billingController.getAllPayments("ADMIN");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(true, body.get("success"));
        assertEquals("All payments retrieved successfully", body.get("message"));
        verify(billingService).getAllPayments();
    }

    @Test
    void testGetAllPayments_NonAdmin() {
        assertThrows(AccessDeniedException.class, () -> {
            billingController.getAllPayments("MANAGER");
        });
    }

    @Test
    void testGetAllPayments_Guest() {
        assertThrows(AccessDeniedException.class, () -> {
            billingController.getAllPayments("GUEST");
        });
    }

    @Test
    void testGetAllPayments_Empty() {
        when(billingService.getAllPayments()).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = billingController.getAllPayments("ADMIN");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(billingService).getAllPayments();
    }
}

