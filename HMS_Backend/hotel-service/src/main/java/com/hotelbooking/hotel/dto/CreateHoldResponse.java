package com.hotelbooking.hotel.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateHoldResponse {

    private String holdId;
    private LocalDateTime expiresAt;
}