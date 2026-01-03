package com.hotelbooking.billing.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MarkBillPaidRequest {

    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // CASH, CARD, UPI, etc.

    private String transactionId;
    private String paymentReference;
    private String notes;
}

