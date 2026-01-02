package com.hotelbooking.booking.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckOutRequest {
    private String notes;
    private Integer rating;
    private String feedback;
    private Boolean lateCheckOut;
}

